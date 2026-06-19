package no.novari.flyt.authorization.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.controller.utils.TokenParsingUtils
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationJwtConverter
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(controllers = [InternalClientUserController::class, UserController::class])
@Import(InternalClientUserControllerTest.TestSecurityConfiguration::class)
class InternalClientUserControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) {
        @MockitoBean
        private lateinit var userService: UserService

        @MockitoBean
        private lateinit var tokenParsingUtils: TokenParsingUtils

        @MockitoBean
        private lateinit var jwtDecoder: JwtDecoder

        @MockitoBean
        private lateinit var sourceApplicationJwtConverter: SourceApplicationJwtConverter

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
                        .with(internalClientJwt()),
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
                        .with(internalClientJwt()),
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
                        .with(internalClientJwt())
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
                        .with(internalClientJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(emptyList<UUID>())),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))
        }

        @Test
        fun `lookup returns bad request for invalid json`() {
            mockMvc
                .perform(
                    post("/api/intern-klient/authorization/users/actions/lookup")
                        .with(internalClientJwt())
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
                        .with(userJwt()),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `admin users endpoint still works for admins`() {
            whenever(tokenParsingUtils.isAdmin(any())).thenReturn(true)
            whenever(userService.getAll(any())).thenReturn(PageImpl(emptyList()))

            mockMvc
                .perform(
                    get("/api/intern/authorization/users")
                        .with(adminJwt()),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(0))
        }

        @Test
        fun `admin users endpoint still requires admin`() {
            whenever(tokenParsingUtils.isAdmin(any())).thenReturn(false)

            mockMvc
                .perform(
                    get("/api/intern/authorization/users")
                        .with(userJwt()),
                ).andExpect(status().isForbidden)
        }

        private fun internalClientJwt() =
            jwt().authorities(SimpleGrantedAuthority("CLIENT_ID_fint-flyt-authorization-oauth2-client"))

        private fun userJwt() = jwt().authorities(SimpleGrantedAuthority("ROLE_USER"))

        private fun adminJwt() =
            jwt().authorities(
                SimpleGrantedAuthority("ROLE_USER"),
                SimpleGrantedAuthority("ROLE_ADMIN"),
            )

        @TestConfiguration
        class TestSecurityConfiguration {
            @Bean
            @Order(1)
            fun internalClientApiFilterChain(http: HttpSecurity): SecurityFilterChain {
                return http
                    .securityMatcher("/api/intern-klient/**")
                    .csrf { it.disable() }
                    .oauth2ResourceServer { it.jwt {} }
                    .authorizeHttpRequests {
                        it.anyRequest().hasAuthority("CLIENT_ID_fint-flyt-authorization-oauth2-client")
                    }.build()
            }

            @Bean
            @Order(2)
            fun internalApiFilterChain(http: HttpSecurity): SecurityFilterChain {
                return http
                    .securityMatcher("/api/intern/**")
                    .csrf { it.disable() }
                    .oauth2ResourceServer { it.jwt {} }
                    .authorizeHttpRequests {
                        it.anyRequest().hasAuthority("ROLE_USER")
                    }.build()
            }

            @Bean
            @Order(3)
            fun fallbackFilterChain(http: HttpSecurity): SecurityFilterChain {
                return http
                    .securityMatcher("/**")
                    .csrf { it.disable() }
                    .authorizeHttpRequests { it.anyRequest().denyAll() }
                    .build()
            }
        }
    }
