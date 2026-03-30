package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import no.novari.kafka.requestreply.ReplyProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component

@Component
class ClientAuthorizationProducerRecordBuilder(
    private val sourceApplications: List<SourceApplication>,
) {
    fun apply(consumerRecord: ConsumerRecord<String, String>): ReplyProducerRecord<ClientAuthorization> {
        val clientId = consumerRecord.value()

        return resolveSourceApplicationId(clientId)?.let { sourceApplicationId ->
            buildReplyProducerRecord(clientId, sourceApplicationId)
        } ?: buildUnauthorizedReplyProducerRecord(clientId)
    }

    private fun buildReplyProducerRecord(
        clientId: String,
        sourceApplicationId: Long,
    ): ReplyProducerRecord<ClientAuthorization> {
        return ReplyProducerRecord
            .builder<ClientAuthorization>()
            .value(
                ClientAuthorization(
                    authorized = true,
                    clientId = clientId,
                    sourceApplicationId = sourceApplicationId,
                ),
            ).build()
    }

    private fun buildUnauthorizedReplyProducerRecord(clientId: String): ReplyProducerRecord<ClientAuthorization> {
        return ReplyProducerRecord
            .builder<ClientAuthorization>()
            .value(
                ClientAuthorization(
                    authorized = false,
                    clientId = clientId,
                ),
            ).build()
    }

    private fun resolveSourceApplicationId(clientId: String): Long? {
        return sourceApplications
            .firstOrNull { sourceApplication -> sourceApplication.clientId == clientId }
            ?.id
    }
}
