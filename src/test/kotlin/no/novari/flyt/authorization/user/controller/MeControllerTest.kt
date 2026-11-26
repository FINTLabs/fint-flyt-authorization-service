package no.novari.flyt.authorization.user.controller

import no.novari.flyt.authorization.support.SecurityHttpTest
import no.novari.flyt.authorization.user.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MeControllerTest : SecurityHttpTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @Test
    fun `restricted page is open to DEVELOPER through the role hierarchy, not to USER`() {
        restrictedPage(developerToken()).andExpect(jsonPath("$.userPermissionPage").value(true))
        restrictedPage(userToken()).andExpect(jsonPath("$.userPermissionPage").value(false))
    }

    @Test
    fun `me rejects a client token`() {
        mockMvc
            .perform(get(URL).header(AUTHORIZATION, "Bearer ${clientToken()}"))
            .andExpect(status().isUnauthorized)
    }

    private fun restrictedPage(token: String) =
        mockMvc
            .perform(get("$URL/restricted-page-authorization").header(AUTHORIZATION, "Bearer $token"))
            .andExpect(status().isOk)

    private companion object {
        const val URL = "/api/intern/authorization/me"
    }
}
