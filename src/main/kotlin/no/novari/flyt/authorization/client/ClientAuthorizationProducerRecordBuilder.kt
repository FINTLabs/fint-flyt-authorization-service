package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import no.novari.kafka.requestreply.ReplyProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ClientAuthorizationProducerRecordBuilder(
    private val sourceApplications: List<SourceApplication>,
) {
    fun apply(consumerRecord: ConsumerRecord<String, String>): ReplyProducerRecord<ClientAuthorization> {
        val clientId = consumerRecord.value()
        logger.debug(
            "Received client authorization request topic={} partition={} offset={} key={} clientId={}",
            consumerRecord.topic(),
            consumerRecord.partition(),
            consumerRecord.offset(),
            consumerRecord.key(),
            clientId,
        )

        val sourceApplication = resolveSourceApplication(clientId)
        return if (sourceApplication != null) {
            logger.debug(
                "Authorized client authorization request clientId={} sourceApplicationId={} sourceApplication={} available={}",
                clientId,
                sourceApplication.id,
                sourceApplication.displayName,
                sourceApplication.available,
            )
            buildReplyProducerRecord(clientId, sourceApplication.id)
        } else {
            logger.debug(
                "Rejected client authorization request clientId={}. Configured source applications: {}",
                clientId,
                sourceApplications.map {
                    "${it.displayName}(id=${it.id}, clientIdConfigured=${it.clientId != null}, available=${it.available})"
                },
            )
            buildUnauthorizedReplyProducerRecord(clientId)
        }
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

    private fun resolveSourceApplication(clientId: String): SourceApplication? {
        return sourceApplications
            .firstOrNull { sourceApplication -> sourceApplication.clientId == clientId }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(ClientAuthorizationProducerRecordBuilder::class.java)
    }
}
