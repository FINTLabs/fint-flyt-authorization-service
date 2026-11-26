package no.novari.flyt.authorization.user.controller

import no.novari.flyt.authorization.support.SecurityHttpTest
import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.model.User
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class InternalClientUserControllerTest : SecurityHttpTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

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
                    .header(AUTHORIZATION, bearer(clientToken())),
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
                    .header(AUTHORIZATION, bearer(clientToken())),
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
                    .header(AUTHORIZATION, bearer(clientToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""["$first", "$second", "$third"]"""),
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
                    .header(AUTHORIZATION, bearer(clientToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[]"),
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
                    .header(AUTHORIZATION, bearer(clientToken()))
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
                    .header(AUTHORIZATION, bearer(clientToken()))
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
                    .header(AUTHORIZATION, bearer(clientToken()))
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
                    .header(AUTHORIZATION, bearer(clientToken()))
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
    fun `authorize source applications rejects a user token`() {
        mockMvc
            .perform(
                post("/api/intern-klient/authorization/users/actions/authorize-source-applications")
                    .header(AUTHORIZATION, bearer(userToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"objectIdentifier":"${UUID.randomUUID()}","sourceApplicationIds":[1]}"""),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `lookup returns bad request for invalid json`() {
        mockMvc
            .perform(
                post("/api/intern-klient/authorization/users/actions/lookup")
                    .header(AUTHORIZATION, bearer(clientToken()))
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
    fun `internal client endpoint rejects a user token`() {
        mockMvc
            .perform(
                get("/api/intern-klient/authorization/users/${UUID.randomUUID()}")
                    .header(AUTHORIZATION, bearer(userToken())),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `source application ids returns the user's ids`() {
        val objectIdentifier = UUID.randomUUID()
        whenever(userService.findSourceApplicationIds(objectIdentifier)).thenReturn(setOf(1L, 3L))

        mockMvc
            .perform(
                get("/api/intern-klient/authorization/users/$objectIdentifier/source-application-ids")
                    .header(AUTHORIZATION, bearer(clientToken())),
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
                    .header(AUTHORIZATION, bearer(clientToken())),
            ).andExpect(status().isOk)
            .andExpect(content().json("[]"))
    }

    @Test
    fun `source application ids rejects another client and user tokens`() {
        val path = "/api/intern-klient/authorization/users/${UUID.randomUUID()}/source-application-ids"
        mockMvc
            .perform(get(path).header(AUTHORIZATION, bearer(clientToken("another-client"))))
            .andExpect(status().isForbidden)
        mockMvc.perform(get(path).header(AUTHORIZATION, bearer(userToken()))).andExpect(status().isUnauthorized)
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized)
    }

    @Test
    fun `admin operations give 403 for USER`() {
        mockMvc
            .perform(get("/api/intern/authorization/users").header(AUTHORIZATION, bearer(userToken())))
            .andExpect(status().isForbidden)
        mockMvc
            .perform(
                post("/api/intern/authorization/users/actions/userPermissionBatchPut")
                    .header(AUTHORIZATION, bearer(userToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[]"),
            ).andExpect(status().isForbidden)
        verifyNoInteractions(userService)
    }

    @Test
    fun `admin operations pass for DEVELOPER`() {
        whenever(userService.getAll(any())).thenReturn(PageImpl(emptyList()))

        for (token in listOf(developerToken())) {
            mockMvc
                .perform(get("/api/intern/authorization/users").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(0))
            mockMvc
                .perform(
                    post("/api/intern/authorization/users/actions/userPermissionBatchPut")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"),
                ).andExpect(status().isOk)
        }
        verify(userService).putAll(emptyList())
    }

    private fun bearer(token: String) = "Bearer $token"
}
