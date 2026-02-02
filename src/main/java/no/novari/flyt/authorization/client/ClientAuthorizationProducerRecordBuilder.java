package no.novari.flyt.authorization.client;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ClientAuthorizationProducerRecordBuilder {

    private final List<SourceApplication> sourceApplications;

    public ClientAuthorizationProducerRecordBuilder(List<SourceApplication> sourceApplications) {
        this.sourceApplications = sourceApplications;
    }

    public ReplyProducerRecord<ClientAuthorization> apply(ConsumerRecord<String, String> consumerRecord) {

        String clientId = consumerRecord.value();

        return resolveSourceApplicationId(clientId)
                .map(sourceApplicationId -> buildReplyProducerRecord(clientId, sourceApplicationId))
                .orElseGet(() -> buildUnauthorizedReplyProducerRecord(clientId));
    }

    private ReplyProducerRecord<ClientAuthorization> buildReplyProducerRecord(String clientId, Long sourceApplicationId) {
        return ReplyProducerRecord.<ClientAuthorization>builder()
                .value(ClientAuthorization
                        .builder()
                        .authorized(true)
                        .clientId(clientId)
                        .sourceApplicationId(sourceApplicationId)
                        .build())
                .build();
    }

    private ReplyProducerRecord<ClientAuthorization> buildUnauthorizedReplyProducerRecord(String clientId) {
        return ReplyProducerRecord.<ClientAuthorization>builder()
                .value(ClientAuthorization
                        .builder()
                        .authorized(false)
                        .clientId(clientId)
                        .build())
                .build();
    }

    private Optional<Long> resolveSourceApplicationId(String clientId) {
        return sourceApplications.stream()
                .filter(sourceApplication -> matchClientId(clientId, sourceApplication.getClientId()))
                .map(SourceApplication::getSourceApplicationId)
                .findFirst();
    }

    private boolean matchClientId(String clientId, String sourceApplicationClientId) {
        return sourceApplicationClientId != null && sourceApplicationClientId.equals(clientId);
    }
}
