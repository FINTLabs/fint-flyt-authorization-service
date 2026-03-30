package no.novari

import no.novari.flyt.authorization.client.ClientAuthorization
import no.novari.flyt.authorization.client.ClientAuthorizationProducerRecordBuilder
import no.novari.flyt.authorization.client.sourceapplications.model.AcosSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.AltinnSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.DigisakSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.EgrunnervervSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.HMSRegSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.VigoSourceApplication
import no.novari.kafka.requestreply.ReplyProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClientAuthorizationProducerRecordBuilderTest {
    @Test
    fun `apply authorizes Acos source application client id`() {
        val clientId = "acosClientId"
        val sourceAppId = 1L
        val builder = builderFor(AcosSourceApplication(clientId, true))

        val result = builder.apply(consumerRecord(clientId))

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `apply authorizes Egrunnerverv source application client id`() {
        val clientId = "egrunnervervClientId"
        val sourceAppId = 2L
        val builder = builderFor(EgrunnervervSourceApplication(clientId, true))

        val result = builder.apply(consumerRecord(clientId))

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `apply authorizes Digisak source application client id`() {
        val clientId = "digisakClientId"
        val sourceAppId = 3L
        val builder = builderFor(DigisakSourceApplication(clientId, true))

        val result = builder.apply(consumerRecord(clientId))

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `apply authorizes Vigo source application client id`() {
        val clientId = "vigoClientId"
        val sourceAppId = 4L
        val builder = builderFor(VigoSourceApplication(clientId, true))

        val result = builder.apply(consumerRecord(clientId))

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `apply authorizes Altinn source application client id`() {
        val clientId = "altinnClientId"
        val sourceAppId = 5L
        val builder = builderFor(AltinnSourceApplication(clientId, true))

        val result = builder.apply(consumerRecord(clientId))

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `apply authorizes HMSReg source application client id`() {
        val clientId = "hmsregClientId"
        val sourceAppId = 6L
        val builder = builderFor(HMSRegSourceApplication(clientId, true))

        val result = builder.apply(consumerRecord(clientId))

        assertAuthorized(result, clientId, sourceAppId)
    }

    @Test
    fun `apply rejects unknown client id`() {
        val clientId = "otherClientId"
        val builder =
            builderFor(
                AcosSourceApplication(null, true),
                EgrunnervervSourceApplication(null, true),
            )

        val result = builder.apply(consumerRecord(clientId))

        assertFalse(result.value.authorized)
        assertEquals(clientId, result.value.clientId)
        assertNull(result.value.sourceApplicationId)
    }

    private fun assertAuthorized(
        result: ReplyProducerRecord<ClientAuthorization>,
        clientId: String,
        sourceApplicationId: Long,
    ) {
        assertTrue(result.value.authorized)
        assertEquals(clientId, result.value.clientId)
        assertEquals(sourceApplicationId, result.value.sourceApplicationId)
    }

    private fun builderFor(vararg sourceApplications: SourceApplication): ClientAuthorizationProducerRecordBuilder {
        return ClientAuthorizationProducerRecordBuilder(sourceApplications.toList())
    }

    private fun consumerRecord(clientId: String): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 0, 0, "", clientId)
    }
}
