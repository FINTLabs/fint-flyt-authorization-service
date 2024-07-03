package no.fintlabs.flyt.authorization.client;

import no.fintlabs.flyt.authorization.client.sourceapplications.AcosSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.DigisakSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.EgrunnervervSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.VigoSourceApplication;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ClientAuthorizationProducerRecordBuilder {

    public ReplyProducerRecord<ClientAuthorization> apply(ConsumerRecord<String, String> consumerRecord) {

        if (AcosSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), AcosSourceApplication.CLIENT_ID)) {
            return buildReplyProducerRecord(AcosSourceApplication.CLIENT_ID, AcosSourceApplication.SOURCE_APPLICATION_ID);
        } else if (EgrunnervervSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), EgrunnervervSourceApplication.CLIENT_ID)) {
            return buildReplyProducerRecord(EgrunnervervSourceApplication.CLIENT_ID, EgrunnervervSourceApplication.SOURCE_APPLICATION_ID);
        } else if (DigisakSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), DigisakSourceApplication.CLIENT_ID)) {
            return buildReplyProducerRecord(DigisakSourceApplication.CLIENT_ID, DigisakSourceApplication.SOURCE_APPLICATION_ID);
        } else if (VigoSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), VigoSourceApplication.CLIENT_ID)) {
            return buildReplyProducerRecord(VigoSourceApplication.CLIENT_ID, VigoSourceApplication.SOURCE_APPLICATION_ID);
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
}
