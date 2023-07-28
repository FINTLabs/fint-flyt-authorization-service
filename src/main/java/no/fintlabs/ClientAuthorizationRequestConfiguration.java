package no.fintlabs;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ClientAuthorizationRequestConfiguration {

    private final ClientAuthorizationProducerRecordBuilder recordBuilder;

    public ClientAuthorizationRequestConfiguration(ClientAuthorizationProducerRecordBuilder recordBuilder) {
        this.recordBuilder = recordBuilder;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> clientAuthorizationRequestConsumer(
            RequestTopicService requestTopicService,
            RequestConsumerFactoryService requestConsumerFactoryService
    ) {
        RequestTopicNameParameters topicNameParameters = RequestTopicNameParameters.builder()
                .resource("authorization")
                .parameterName("client-id")
                .build();

        requestTopicService.ensureTopic(topicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                String.class,
                ClientAuthorization.class,
                (recordBuilder::apply),
                new CommonLoggingErrorHandler()

        ).createContainer(topicNameParameters);
    }

}
