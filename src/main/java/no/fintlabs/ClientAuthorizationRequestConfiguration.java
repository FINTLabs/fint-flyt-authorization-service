package no.fintlabs;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ClientAuthorizationRequestConfiguration {

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
                (consumerRecord) -> ReplyProducerRecord.<ClientAuthorization>builder()
                        .value(
                                consumerRecord.value().equals("5679f546-b72e-41d4-bbfe-68b029a8c158")
                                        ?
                                        ClientAuthorization
                                                .builder()
                                                .authorized(true)
                                                .clientId(consumerRecord.value())
                                                .sourceApplicationId("1")
                                                .build()
                                        :
                                        ClientAuthorization
                                                .builder()
                                                .authorized(false)
                                                .clientId(consumerRecord.value())
                                                .build()
                        ).build(),
                new CommonLoggingErrorHandler()
        ).createContainer(topicNameParameters);
    }

}
