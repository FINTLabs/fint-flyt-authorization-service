package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.client.sourceapplications.model.AcosSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.AltinnSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.DigisakSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.EgrunnervervSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.HMSRegSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.VigoSourceApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ClientAuthorizationServiceTest {
    @Test
    fun `find authorizes Acos source application client id`() {
        val clientId = "acosClientId"
        val sourceAppId = 1L
        val service = serviceFor(AcosSourceApplication(clientId, true))

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Egrunnerverv source application client id`() {
        val clientId = "egrunnervervClientId"
        val sourceAppId = 2L
        val service = serviceFor(EgrunnervervSourceApplication(clientId, true))

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Digisak source application client id`() {
        val clientId = "digisakClientId"
        val sourceAppId = 3L
        val service = serviceFor(DigisakSourceApplication(clientId, true))

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Vigo source application client id`() {
        val clientId = "vigoClientId"
        val sourceAppId = 4L
        val service = serviceFor(VigoSourceApplication(clientId, true))

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes Altinn source application client id`() {
        val clientId = "altinnClientId"
        val sourceAppId = 5L
        val service = serviceFor(AltinnSourceApplication(clientId, true))

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find authorizes HMSReg source application client id`() {
        val clientId = "hmsregClientId"
        val sourceAppId = 6L
        val service = serviceFor(HMSRegSourceApplication(clientId, true))

        val result = service.find(clientId)

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `find returns null for unknown client id`() {
        val clientId = "otherClientId"
        val service =
            serviceFor(
                AcosSourceApplication(null, true),
                EgrunnervervSourceApplication(null, true),
            )

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

    private fun serviceFor(vararg sourceApplications: SourceApplication) =
        ClientAuthorizationService(sourceApplications.toList())
}
