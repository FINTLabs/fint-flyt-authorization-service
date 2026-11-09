package no.novari.flyt.authorization.client

import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationJwtConverter
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [InternalClientAuthorizationController::class])
@Import(InternalClientAuthorizationControllerTest.TestSecurityConfiguration::class)
class InternalClientAuthorizationControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockitoBean
        private lateinit var clientAuthorizationService: ClientAuthorizationService

        @MockitoBean
        private lateinit var jwtDecoder: JwtDecoder

        @MockitoBean
        private lateinit var sourceApplicationJwtConverter: SourceApplicationJwtConverter

        @Test
        fun `get returns client authorization when client is registered`() {
            whenever(clientAuthorizationService.find("acosClientId")).thenReturn(
                ClientAuthorization(authorized = true, clientId = "acosClientId", sourceApplicationId = 1L),
            )

            mockMvc
                .perform(get(URL, "acosClientId").with(internalClientJwt()))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.authorized").value(true))
                .andExpect(jsonPath("$.clientId").value("acosClientId"))
                .andExpect(jsonPath("$.sourceApplicationId").value(1))
        }

        @Test
        fun `get returns not found for unknown client`() {
            whenever(clientAuthorizationService.find("unknownClientId")).thenReturn(null)

            mockMvc
                .perform(get(URL, "unknownClientId").with(internalClientJwt()))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `get returns unauthorized without authorization header`() {
            mockMvc
                .perform(get(URL, "acosClientId"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `get rejects token from other client`() {
            mockMvc
                .perform(
                    get(URL, "acosClientId")
                        .with(jwt().authorities(SimpleGrantedAuthority("CLIENT_ID_other-client"))),
                ).andExpect(status().isForbidden)
        }

        private fun internalClientJwt() =
            jwt().authorities(SimpleGrantedAuthority("CLIENT_ID_fint-flyt-authorization-oauth2-client"))

        @TestConfiguration
        class TestSecurityConfiguration {
            @Bean
            fun internalClientApiFilterChain(http: HttpSecurity): SecurityFilterChain =
                http
                    .securityMatcher("/api/intern-klient/**")
                    .csrf { it.disable() }
                    .oauth2ResourceServer { it.jwt {} }
                    .authorizeHttpRequests {
                        it.anyRequest().hasAuthority("CLIENT_ID_fint-flyt-authorization-oauth2-client")
                    }.build()
        }

        private companion object {
            const val URL = "/api/intern-klient/authorization/clients/{clientId}"
        }
    }
