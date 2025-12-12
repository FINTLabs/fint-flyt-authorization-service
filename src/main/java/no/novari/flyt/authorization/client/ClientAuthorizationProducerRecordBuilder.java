package no.novari.flyt.authorization.client;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.authorization.client.sourceapplications.*;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class ClientAuthorizationProducerRecordBuilder {

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
        return Stream.of(
                        matchClientId(clientId, AcosSourceApplication.CLIENT_ID, AcosSourceApplication.SOURCE_APPLICATION_ID),
                        matchClientId(clientId, EgrunnervervSourceApplication.CLIENT_ID, EgrunnervervSourceApplication.SOURCE_APPLICATION_ID),
                        matchClientId(clientId, DigisakSourceApplication.CLIENT_ID, DigisakSourceApplication.SOURCE_APPLICATION_ID),
                        matchClientId(clientId, VigoSourceApplication.CLIENT_ID, VigoSourceApplication.SOURCE_APPLICATION_ID),
                        matchClientId(clientId, AltinnSourceApplication.CLIENT_ID, AltinnSourceApplication.SOURCE_APPLICATION_ID),
                        matchClientId(clientId, HMSRegSourceApplication.CLIENT_ID, HMSRegSourceApplication.SOURCE_APPLICATION_ID),
                        matchClientId(clientId, IsyGravingSourceApplication.CLIENT_ID, IsyGravingSourceApplication.SOURCE_APPLICATION_ID)
                )
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<Long> matchClientId(String clientId, String sourceApplicationClientId, Long sourceApplicationId) {
        if (sourceApplicationClientId == null || !sourceApplicationClientId.equals(clientId)) {
            return Optional.empty();
        }
        return Optional.of(sourceApplicationId);
    }
}
