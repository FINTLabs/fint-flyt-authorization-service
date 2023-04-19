package no.fintlabs;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.models.AcosSourceApplication;
import no.fintlabs.models.EgrunnervervSourceApplication;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ClientAuthorizationRequestConfiguration {

    private static ReplyProducerRecord<ClientAuthorization> apply(ConsumerRecord<String, String> consumerRecord) {

        return switch (consumerRecord.value()) {
            case AcosSourceApplication.CLIENT_ID -> ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                            .builder()
                            .authorized(true)
                            .clientId(consumerRecord.value())
                            .sourceApplicationId(AcosSourceApplication.SOURCE_APPLICATION_ID)
                            .build())
                    .build();
            case EgrunnervervSourceApplication.CLIENT_ID -> ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                            .builder()
                            .authorized(true)
                            .clientId(consumerRecord.value())
                            .sourceApplicationId(EgrunnervervSourceApplication.SOURCE_APPLICATION_ID)
                            .build())
                    .build();
            default -> ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                            .builder()
                            .authorized(false)
                            .clientId(consumerRecord.value())
                            .build())
                    .build();
        };

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
                (ClientAuthorizationRequestConfiguration::apply),
                new CommonLoggingErrorHandler()

        ).createContainer(topicNameParameters);
    }

}
