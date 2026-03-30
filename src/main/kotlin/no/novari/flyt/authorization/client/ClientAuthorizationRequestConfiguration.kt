package no.novari.flyt.authorization.client

import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.requestreply.ReplyProducerRecord
import no.novari.kafka.requestreply.RequestListenerConfiguration
import no.novari.kafka.requestreply.RequestListenerContainerFactory
import no.novari.kafka.requestreply.topic.RequestTopicService
import no.novari.kafka.requestreply.topic.configuration.RequestTopicConfiguration
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.time.Duration
import java.util.function.Function

@Configuration
class ClientAuthorizationRequestConfiguration(
    private val recordBuilder: ClientAuthorizationProducerRecordBuilder,
) {
    @Bean
    fun clientAuthorizationRequestConsumer(
        requestTopicService: RequestTopicService,
        requestListenerContainerFactory: RequestListenerContainerFactory,
        errorHandlerFactory: ErrorHandlerFactory,
    ): ConcurrentMessageListenerContainer<String, String> {
        val topicNameParameters =
            RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).resourceName("authorization")
                .parameterName("client-id")
                .build()

        requestTopicService.createOrModifyTopic(
            topicNameParameters,
            RequestTopicConfiguration
                .builder()
                .retentionTime(RETENTION_TIME)
                .build(),
        )

        val replyProducer =
            Function<ConsumerRecord<String, String>, ReplyProducerRecord<ClientAuthorization>> { consumerRecord ->
                recordBuilder.apply(consumerRecord)
            }

        return requestListenerContainerFactory
            .createRecordConsumerFactory(
                String::class.java,
                ClientAuthorization::class.java,
                replyProducer,
                RequestListenerConfiguration
                    .stepBuilder(String::class.java)
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .build(),
                errorHandlerFactory.createErrorHandler(
                    ErrorHandlerConfiguration
                        .stepBuilder<String>()
                        .noRetries()
                        .skipFailedRecords()
                        .build(),
                ),
            ).createContainer(topicNameParameters)
    }

    private companion object {
        val RETENTION_TIME: Duration = Duration.ofMinutes(10)
    }
}
