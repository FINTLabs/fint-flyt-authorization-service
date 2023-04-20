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

import java.util.Objects;

@Configuration
public class ClientAuthorizationRequestConfiguration {


    private static ReplyProducerRecord<ClientAuthorization> apply(ConsumerRecord<String, String> consumerRecord) {

        if (Objects.equals(consumerRecord.value(), AcosSourceApplication.CLIENT_ID)){
            return buildReplyProducerRecord(AcosSourceApplication.CLIENT_ID, AcosSourceApplication.SOURCE_APPLICATION_ID);
        } else if (Objects.equals(consumerRecord.value(), EgrunnervervSourceApplication.CLIENT_ID)){
            return buildReplyProducerRecord(EgrunnervervSourceApplication.CLIENT_ID, EgrunnervervSourceApplication.SOURCE_APPLICATION_ID);
        } else {
            return ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                            .builder()
                            .authorized(false)
                            .clientId(consumerRecord.value())
                            .build())
                    .build();
        }
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

    private static ReplyProducerRecord<ClientAuthorization> buildReplyProducerRecord(String clientId, String sourceApplicationId){
        return ReplyProducerRecord.<ClientAuthorization>builder()
                .value(ClientAuthorization
                        .builder()
                        .authorized(true)
                        .clientId(clientId)
                        .sourceApplicationId(sourceApplicationId)
                        .build())
                .build();
    }

}
