package no.novari.flyt.authorization.client;

import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.requestreply.RequestListenerConfiguration;
import no.novari.kafka.requestreply.RequestListenerContainerFactory;
import no.novari.kafka.requestreply.topic.RequestTopicService;
import no.novari.kafka.requestreply.topic.configuration.RequestTopicConfiguration;
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
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
                        .stepBuilder()
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
