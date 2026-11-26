package no.novari.flyt.authorization.client.sourceapplications

import no.novari.flyt.authorization.support.SecurityHttpTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SourceApplicationControllerTest : SecurityHttpTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `lists the source applications as the component classes did, with their default availability`() {
        mockMvc
            .perform(get("/api/intern/authorization/sourceapplications").header(AUTHORIZATION, "Bearer ${userToken()}"))
            .andExpect(status().isOk)
            .andExpect(content().json(EXPECTED, true))
    }

    private companion object {
        // The response from main before the enum, without overlay env.
        const val EXPECTED = """[
            {"id": 1, "displayName": "Acos Interact", "available": false},
            {"id": 5, "displayName": "Altinn", "available": true},
            {"id": 11, "displayName": "Conexus", "available": false},
            {"id": 3, "displayName": "Digisak", "available": false},
            {"id": 10, "displayName": "eApply", "available": false},
            {"id": 2, "displayName": "eGrunnerverv", "available": true},
            {"id": 8, "displayName": "FSkyss", "available": false},
            {"id": 6, "displayName": "HMSReg", "available": true},
            {"id": 7, "displayName": "ISY Graving", "available": false},
            {"id": 9, "displayName": "SiDE", "available": false},
            {"id": 4, "displayName": "VIGO", "available": true}
        ]"""
    }
}
