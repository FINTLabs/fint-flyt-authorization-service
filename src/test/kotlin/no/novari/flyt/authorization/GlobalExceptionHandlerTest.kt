package no.novari.flyt.authorization

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

class GlobalExceptionHandlerTest {
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(ThrowingController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

    @Test
    fun `response status exception preserves status and reason as problem detail`() {
        mockMvc
            .perform(get("/test/response-status-forbidden"))
            .andExpect(status().isForbidden)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.detail").value("No access"))
    }

    @Test
    fun `illegal argument returns 400 without leaking internal message`() {
        mockMvc
            .perform(get("/test/illegal-argument"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid or incomplete authentication token"))
    }

    @Test
    fun `path variable type mismatch returns 400 problem detail`() {
        mockMvc
            .perform(get("/test/path/not-a-number"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid value for request parameter 'id'"))
    }

    @Test
    fun `unhandled exception returns generic 500 problem detail`() {
        mockMvc
            .perform(get("/test/generic"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
    }

    @RestController
    private class ThrowingController {
        @GetMapping("/test/response-status-forbidden")
        fun responseStatus(): Nothing = throw ResponseStatusException(HttpStatus.FORBIDDEN, "No access")

        @GetMapping("/test/illegal-argument")
        fun illegalArgument(): Nothing = throw IllegalArgumentException("Missing token claim: objectIdentifier")

        @GetMapping("/test/generic")
        fun generic(): Nothing = throw RuntimeException("boom")

        @GetMapping("/test/path/{id}")
        fun path(
            @PathVariable id: Long,
        ): Long = id
    }
}
