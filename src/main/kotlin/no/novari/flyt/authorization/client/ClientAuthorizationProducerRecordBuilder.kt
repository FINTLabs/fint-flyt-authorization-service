package no.novari.flyt.authorization.client

import no.novari.kafka.requestreply.ReplyProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component

@Component
class ClientAuthorizationProducerRecordBuilder(
    private val clientAuthorizationService: ClientAuthorizationService,
) {
    fun apply(consumerRecord: ConsumerRecord<String, String>): ReplyProducerRecord<ClientAuthorization> {
        val clientId = consumerRecord.value()
        val clientAuthorization =
            clientAuthorizationService.find(clientId)
                ?: ClientAuthorization(authorized = false, clientId = clientId)

        return ReplyProducerRecord
            .builder<ClientAuthorization>()
            .value(clientAuthorization)
            .build()
    }
}
