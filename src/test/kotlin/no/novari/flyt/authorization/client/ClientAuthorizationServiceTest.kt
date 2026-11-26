package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.client.sourceapplications.SourceApplications
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class ClientAuthorizationServiceTest {
    @Test
    fun `find authorizes Acos source application client id`() {
        val clientId = "acosClientId"
        val sourceAppId = 1L
        val service = serviceFor("acos" to clientId)

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Egrunnerverv source application client id`() {
        val clientId = "egrunnervervClientId"
        val sourceAppId = 2L
        val service = serviceFor("egrunnerverv" to clientId)

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Digisak source application client id`() {
        val clientId = "digisakClientId"
        val sourceAppId = 3L
        val service = serviceFor("digisak" to clientId)

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Vigo source application client id`() {
        val clientId = "vigoClientId"
        val sourceAppId = 4L
        val service = serviceFor("vigo" to clientId)

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Altinn source application client id`() {
        val clientId = "altinnClientId"
        val sourceAppId = 5L
        val service = serviceFor("altinn" to clientId)

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes HMSReg source application client id`() {
        val clientId = "hmsregClientId"
        val sourceAppId = 6L
        val service = serviceFor("hmsreg" to clientId)

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find returns null for unknown client id`() {
        val clientId = "otherClientId"
        val service =
            serviceFor("acos" to "acosClientId")

        val result = service.find(clientId)

        assertNull(result)
    }

    private fun assertAuthorized(
        result: ClientAuthorization?,
        clientId: String,
        sourceApplicationId: Long,
    ) {
        assertEquals(
            ClientAuthorization(authorized = true, clientId = clientId, sourceApplicationId = sourceApplicationId),
            result,
        )
    }

    private fun serviceFor(vararg clientIds: Pair<String, String>): ClientAuthorizationService {
        val environment = MockEnvironment()
        clientIds.forEach { (app, clientId) -> environment.setProperty("fint.flyt.$app.sso.client-id", clientId) }
        return ClientAuthorizationService(SourceApplications(environment))
    }
}
