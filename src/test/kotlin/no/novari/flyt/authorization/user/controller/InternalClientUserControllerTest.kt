package no.novari.flyt.authorization.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.authorization.config.MethodSecurityConfiguration
import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.webresourceserver.security.AuthorityMappingService
import no.novari.flyt.webresourceserver.security.InternalClientApiConfiguration
import no.novari.flyt.webresourceserver.security.InternalUserApiConfiguration
import no.novari.flyt.webresourceserver.security.SecurityConfiguration
import no.novari.flyt.webresourceserver.security.SecurityFilterChainFactoryService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationJwtConverter
import no.novari.flyt.webresourceserver.security.user.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * Runs the production filter chains and role mapping from flyt-web-resource-server, configured through properties as
 * in the overlays. Only the JwtDecoder is stubbed: [USER] and [DEVELOPER] decode to user tokens with role claims, and
 * any other token decodes to a client token with that subject.
 */
@WebMvcTest(controllers = [InternalClientUserController::class, UserController::class])
@ImportAutoConfiguration(
    SecurityConfiguration::class,
    InternalClientApiConfiguration::class,
    InternalUserApiConfiguration::class,
)
@Import(AuthorityMappingService::class, SecurityFilterChainFactoryService::class, MethodSecurityConfiguration::class)
@TestPropertySource(
    properties = [
        "fint.flyt.authorization.sso.client-id=$INTERNAL_CLIENT_ID",
        "novari.flyt.web-resource-server.security.api.internal.enabled=true",
        "novari.flyt.web-resource-server.security.api.internal.authorized-org-id-role-pairs-json=" +
            "{\"fintlabs.no\":[\"USER\"],\"vigo.no\":[\"DEVELOPER\",\"USER\"]}",
        "novari.flyt.web-resource-server.security.api.internal-client.enabled=true",
        "novari.flyt.web-resource-server.security.api.internal-client.authorized-client-ids=" +
            "\${fint.flyt.authorization.sso.client-id}",
    ],
)
class InternalClientUserControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) {
        @MockitoBean
        private lateinit var userService: UserService

        @MockitoBean
        private lateinit var jwtDecoder: JwtDecoder

        // Picked up by @WebMvcTest as a Converter bean; only used by the external API filter chain.
        @MockitoBean
        private lateinit var sourceApplicationJwtConverter: SourceApplicationJwtConverter

        @BeforeEach
        fun stubJwtDecoder() {
            whenever(jwtDecoder.decode(any())).thenAnswer { invocation ->
                when (val token = invocation.getArgument<String>(0)) {
                    USER -> {
                        userJwt(token, "fintlabs.no", UserRole.USER)
                    }

                    DEVELOPER -> {
                        userJwt(token, "vigo.no", UserRole.DEVELOPER)
                    }

                    "invalid" -> {
                        throw BadJwtException("Invalid token")
                    }

                    else -> {
                        Jwt
                            .withTokenValue(token)
                            .header("alg", "none")
                            .subject(token)
                            .build()
                    }
                }
            }
        }

        @Test
        fun `get returns user json when found`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(userService.find(objectIdentifier)).thenReturn(
                User(
                    objectIdentifier = objectIdentifier,
                    name = "Ada Lovelace",
                    email = "ada@example.no",
                ),
            )

            mockMvc
                .perform(
                    get("/api/intern-klient/authorization/users/$objectIdentifier")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.objectIdentifier").value(objectIdentifier.toString()))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.no"))
        }

        @Test
        fun `get returns not found when user is missing`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(userService.find(objectIdentifier)).thenReturn(null)

            mockMvc
                .perform(
                    get("/api/intern-klient/authorization/users/$objectIdentifier")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `lookup returns matching users only`() {
            val first = UUID.randomUUID()
            val second = UUID.randomUUID()
            val third = UUID.randomUUID()
            whenever(userService.findAllByObjectIdentifiers(listOf(first, second, third))).thenReturn(
                listOf(
                    User(objectIdentifier = first, name = "Ada Lovelace"),
                    User(objectIdentifier = third, name = "Grace Hopper"),
                ),
            )

            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/lookup")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(listOf(first, second, third))),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].objectIdentifier").value(first.toString()))
                .andExpect(jsonPath("$[0].name").value("Ada Lovelace"))
                .andExpect(jsonPath("$[1].objectIdentifier").value(third.toString()))
                .andExpect(jsonPath("$[1].name").value("Grace Hopper"))
        }

        @Test
        fun `lookup returns empty list for empty input`() {
            whenever(userService.findAllByObjectIdentifiers(emptyList())).thenReturn(emptyList())

            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/lookup")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(emptyList<UUID>())),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))
        }

        @Test
        fun `authorize source applications returns the requested permitted IDs once`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(
                userService.findAuthorizedSourceApplicationIds(
                    objectIdentifier,
                    setOf(1L, 2L, 3L),
                ),
            ).thenReturn(setOf(2L, 3L))

            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "objectIdentifier": "$objectIdentifier",
                              "sourceApplicationIds": [1, 2, 2, 3]
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.authorizedSourceApplicationIds.length()").value(2))
                .andExpect(jsonPath("$.authorizedSourceApplicationIds[0]").value(2))
                .andExpect(jsonPath("$.authorizedSourceApplicationIds[1]").value(3))
        }

        @Test
        fun `authorize source applications returns empty list for an unknown user`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(
                userService.findAuthorizedSourceApplicationIds(objectIdentifier, setOf(1L, 2L)),
            ).thenReturn(emptySet())

            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "objectIdentifier": "$objectIdentifier",
                              "sourceApplicationIds": [1, 2]
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.authorizedSourceApplicationIds.length()").value(0))
        }

        @Test
        fun `authorize source applications returns empty list when no requested ID is permitted`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(
                userService.findAuthorizedSourceApplicationIds(objectIdentifier, setOf(1L, 2L)),
            ).thenReturn(emptySet())

            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "objectIdentifier": "$objectIdentifier",
                              "sourceApplicationIds": [1, 2]
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.authorizedSourceApplicationIds.length()").value(0))
        }

        @Test
        fun `authorize source applications returns empty list for empty input`() {
            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {
                              "objectIdentifier": "${UUID.randomUUID()}",
                              "sourceApplicationIds": []
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.authorizedSourceApplicationIds.length()").value(0))
        }

        @Test
        fun `authorize source applications returns unauthorized without authorization header`() {
            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"objectIdentifier":"${UUID.randomUUID()}","sourceApplicationIds":[1]}"""),
                ).andExpect(status().isUnauthorized)
        }

        @Test
        fun `authorize source applications rejects ordinary user token`() {
            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                        .header(AUTHORIZATION, bearer(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"objectIdentifier":"${UUID.randomUUID()}","sourceApplicationIds":[1]}"""),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `lookup returns bad request for invalid json`() {
            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/lookup")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `internal client endpoint returns unauthorized without authorization header`() {
            mockMvc
                .perform(get("/api/intern-klient/authorization/users/${UUID.randomUUID()}"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `internal client endpoint rejects ordinary user token`() {
            mockMvc
                .perform(
                    get("/api/intern-klient/authorization/users/${UUID.randomUUID()}")
                        .header(AUTHORIZATION, bearer(USER)),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `source application ids returns the user's ids`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(userService.findSourceApplicationIds(objectIdentifier)).thenReturn(setOf(1L, 3L))

            mockMvc
                .perform(
                    get("/api/intern-klient/authorization/users/$objectIdentifier/source-application-ids")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID)),
                ).andExpect(status().isOk)
                .andExpect(content().json("[1, 3]"))
        }

        @Test
        fun `source application ids returns an empty list for an unknown user`() {
            val objectIdentifier = UUID.randomUUID()
            whenever(userService.findSourceApplicationIds(objectIdentifier)).thenReturn(emptySet())

            mockMvc
                .perform(
                    get("/api/intern-klient/authorization/users/$objectIdentifier/source-application-ids")
                        .header(AUTHORIZATION, bearer(INTERNAL_CLIENT_ID)),
                ).andExpect(status().isOk)
                .andExpect(content().json("[]"))
        }

        @Test
        fun `source application ids rejects another client and user tokens`() {
            val path = "/api/intern-klient/authorization/users/${UUID.randomUUID()}/source-application-ids"
            mockMvc
                .perform(get(path).header(AUTHORIZATION, bearer("another-client")))
                .andExpect(status().isForbidden)
            mockMvc.perform(get(path).header(AUTHORIZATION, bearer(USER))).andExpect(status().isForbidden)
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized)
        }

        @Test
        fun `admin operations give 403 for USER`() {
            mockMvc
                .perform(get("/api/intern/authorization/users").header(AUTHORIZATION, bearer(USER)))
                .andExpect(status().isForbidden)
            mockMvc
                .perform(
                    post("/api/intern/authorization/users/actions/userPermissionBatchPut")
                        .header(AUTHORIZATION, bearer(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"),
                ).andExpect(status().isForbidden)
            verifyNoInteractions(userService)
        }

        @Test
        fun `admin operations pass for DEVELOPER, who gets ADMIN from the role mapping`() {
            whenever(userService.getAll(any())).thenReturn(PageImpl(emptyList()))

            run {
                mockMvc
                    .perform(get("/api/intern/authorization/users").header(AUTHORIZATION, bearer(DEVELOPER)))
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.content.length()").value(0))
                mockMvc
                    .perform(
                        post("/api/intern/authorization/users/actions/userPermissionBatchPut")
                            .header(AUTHORIZATION, bearer(DEVELOPER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"),
                    ).andExpect(status().isOk)
            }
            verify(userService).putAll(emptyList())
        }

        private fun bearer(token: String) = "Bearer $token"

        private fun userJwt(
            token: String,
            organizationId: String,
            role: UserRole,
        ): Jwt =
            Jwt
                .withTokenValue(token)
                .header("alg", "none")
                .subject("opaque-user-id")
                .claim("organizationid", organizationId)
                .claim("objectidentifier", UUID.randomUUID().toString())
                .claim("roles", listOf(role.claimValue))
                .build()

        private companion object {
            const val USER = "user-token"
            const val DEVELOPER = "developer-token"
        }
    }

private const val INTERNAL_CLIENT_ID = "fint-flyt-authorization-oauth2-client"
