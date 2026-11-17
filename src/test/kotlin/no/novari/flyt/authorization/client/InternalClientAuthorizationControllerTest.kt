package no.novari.flyt.authorization.client

import no.novari.flyt.webresourceserver.security.AuthorityMappingService
import no.novari.flyt.webresourceserver.security.InternalClientApiConfiguration
import no.novari.flyt.webresourceserver.security.SecurityConfiguration
import no.novari.flyt.webresourceserver.security.SecurityFilterChainFactoryService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationJwtConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Runs the production internal client security filter chain from flyt-web-resource-server, so the client ID check
 * is configured through properties exactly as in application.yaml. Only the JwtDecoder is stubbed:
 * the token value is used as the JWT subject, and "invalid" makes decoding fail.
 */
@WebMvcTest(controllers = [InternalClientAuthorizationController::class])
@ImportAutoConfiguration(SecurityConfiguration::class, InternalClientApiConfiguration::class)
@Import(AuthorityMappingService::class, SecurityFilterChainFactoryService::class)
@TestPropertySource(
    properties = [
        "fint.flyt.authorization.sso.client-id=$AUTHORIZED_CLIENT_ID",
        "novari.flyt.web-resource-server.security.api.internal-client.enabled=true",
        "novari.flyt.web-resource-server.security.api.internal-client.authorized-client-ids=" +
            "\${fint.flyt.authorization.sso.client-id}",
    ],
)
class InternalClientAuthorizationControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockitoBean
        private lateinit var clientAuthorizationService: ClientAuthorizationService

        @MockitoBean
        private lateinit var jwtDecoder: JwtDecoder

        // Picked up by @WebMvcTest as a Converter bean; only used by the external API filter chain.
        @MockitoBean
        private lateinit var sourceApplicationJwtConverter: SourceApplicationJwtConverter

        @BeforeEach
        fun stubJwtDecoder() {
            whenever(jwtDecoder.decode(any())).thenAnswer { invocation ->
                val token = invocation.getArgument<String>(0)
                if (token == INVALID_TOKEN) throw BadJwtException("Invalid token")
                Jwt
                    .withTokenValue(token)
                    .header("alg", "none")
                    .subject(token)
                    .build()
            }
        }

        @Test
        fun `get returns client authorization when client is registered`() {
            whenever(clientAuthorizationService.find("acosClientId")).thenReturn(
                ClientAuthorization(authorized = true, clientId = "acosClientId", sourceApplicationId = 1L),
            )

            mockMvc
                .perform(get(URL, "acosClientId").header(HttpHeaders.AUTHORIZATION, bearer(AUTHORIZED_CLIENT_ID)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.authorized").value(true))
                .andExpect(jsonPath("$.clientId").value("acosClientId"))
                .andExpect(jsonPath("$.sourceApplicationId").value(1))
        }

        @Test
        fun `get returns not found for unknown client`() {
            whenever(clientAuthorizationService.find("unknownClientId")).thenReturn(null)

            mockMvc
                .perform(get(URL, "unknownClientId").header(HttpHeaders.AUTHORIZATION, bearer(AUTHORIZED_CLIENT_ID)))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `get returns unauthorized without token`() {
            mockMvc
                .perform(get(URL, "acosClientId"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `get returns unauthorized for invalid token`() {
            mockMvc
                .perform(get(URL, "acosClientId").header(HttpHeaders.AUTHORIZATION, bearer(INVALID_TOKEN)))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `get rejects token from other client`() {
            mockMvc
                .perform(get(URL, "acosClientId").header(HttpHeaders.AUTHORIZATION, bearer("other-client")))
                .andExpect(status().isForbidden)
        }

        private fun bearer(token: String) = "Bearer $token"

        private companion object {
            const val URL = "/api/intern-klient/authorization/clients/{clientId}"
            const val INVALID_TOKEN = "invalid"
        }
    }

private const val AUTHORIZED_CLIENT_ID = "fint-flyt-authorization-oauth2-client"
