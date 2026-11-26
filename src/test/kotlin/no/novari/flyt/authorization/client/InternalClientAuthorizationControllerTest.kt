package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.support.SecurityHttpTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class InternalClientAuthorizationControllerTest : SecurityHttpTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var clientAuthorizationService: ClientAuthorizationService

    @Test
    fun `get returns client authorization when client is registered`() {
        whenever(clientAuthorizationService.find("acosClientId")).thenReturn(
            ClientAuthorization(authorized = true, clientId = "acosClientId", sourceApplicationId = 1L),
        )

        mockMvc
            .perform(get(URL, "acosClientId").header(AUTHORIZATION, bearer(clientToken())))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.authorized").value(true))
            .andExpect(jsonPath("$.clientId").value("acosClientId"))
            .andExpect(jsonPath("$.sourceApplicationId").value(1))
    }

    @Test
    fun `get returns not found for unknown client`() {
        whenever(clientAuthorizationService.find("unknownClientId")).thenReturn(null)

        mockMvc
            .perform(get(URL, "unknownClientId").header(AUTHORIZATION, bearer(clientToken())))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get returns unauthorized without token or with an invalid token`() {
        mockMvc.perform(get(URL, "acosClientId")).andExpect(status().isUnauthorized)
        mockMvc
            .perform(get(URL, "acosClientId").header(AUTHORIZATION, bearer("invalid")))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get rejects token from other client`() {
        mockMvc
            .perform(get(URL, "acosClientId").header(AUTHORIZATION, bearer(clientToken("other-client"))))
            .andExpect(status().isForbidden)
    }

    private fun bearer(token: String) = "Bearer $token"

    private companion object {
        const val URL = "/api/intern-klient/authorization/clients/{clientId}"
    }
}
