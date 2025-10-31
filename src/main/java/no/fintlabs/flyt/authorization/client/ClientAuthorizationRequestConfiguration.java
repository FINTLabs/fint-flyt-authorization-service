package no.fintlabs.flyt.authorization.client;

import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ErrorHandlerFactory;
import no.fintlabs.kafka.requestreply.RequestListenerConfiguration;
import no.fintlabs.kafka.requestreply.RequestListenerContainerFactory;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.kafka.requestreply.topic.configuration.RequestTopicConfiguration;
import no.fintlabs.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;

@Configuration
public class ClientAuthorizationRequestConfiguration {

    private final ClientAuthorizationProducerRecordBuilder recordBuilder;

    private static final Duration RETENTION_TIME = Duration.ofMinutes(10);

    public ClientAuthorizationRequestConfiguration(ClientAuthorizationProducerRecordBuilder recordBuilder) {
        this.recordBuilder = recordBuilder;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> clientAuthorizationRequestConsumer(
            RequestTopicService requestTopicService,
            RequestListenerContainerFactory requestListenerContainerFactory,
            ErrorHandlerFactory errorHandlerFactory) {
        RequestTopicNameParameters topicNameParameters = RequestTopicNameParameters.builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("authorization")
                .parameterName("client-id")
                .build();

        requestTopicService.createOrModifyTopic(topicNameParameters, RequestTopicConfiguration.builder()
                .retentionTime(RETENTION_TIME)
                .build()
        );

        return requestListenerContainerFactory.createRecordConsumerFactory(
                String.class,
                ClientAuthorization.class,
                (recordBuilder::apply),
                RequestListenerConfiguration
                        .stepBuilder(String.class)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build())

        ).createContainer(topicNameParameters);
    }

}
