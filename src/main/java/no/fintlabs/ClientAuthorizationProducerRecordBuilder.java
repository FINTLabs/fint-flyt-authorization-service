package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.models.sourceapplication.AcosSourceApplication;
import no.fintlabs.models.sourceapplication.EgrunnervervSourceApplication;
import no.fintlabs.models.sourceapplication.DigisakSourceApplication;
import no.fintlabs.models.sourceapplication.VigoSourceApplication;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class ClientAuthorizationProducerRecordBuilder {

    public ReplyProducerRecord<ClientAuthorization> apply(ConsumerRecord<String, String> consumerRecord) {

        if (AcosSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), AcosSourceApplication.CLIENT_ID)) {
            return buildReplyProducerRecord(AcosSourceApplication.CLIENT_ID, AcosSourceApplication.SOURCE_APPLICATION_ID);
        } else if (EgrunnervervSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), EgrunnervervSourceApplication.CLIENT_ID)) {
            return buildReplyProducerRecord(EgrunnervervSourceApplication.CLIENT_ID, EgrunnervervSourceApplication.SOURCE_APPLICATION_ID);
        } else if (DigisakSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), DigisakSourceApplication.CLIENT_ID)) {
            log.debug("Request by client with id="+ DigisakSourceApplication.CLIENT_ID+". Returning source application id="+ DigisakSourceApplication.SOURCE_APPLICATION_ID);
            return buildReplyProducerRecord(DigisakSourceApplication.CLIENT_ID, DigisakSourceApplication.SOURCE_APPLICATION_ID);
        } else if (VigoSourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), VigoSourceApplication.CLIENT_ID)) {
            log.debug("Request by client with id="+ VigoSourceApplication.CLIENT_ID+". Returning source application id="+ VigoSourceApplication.SOURCE_APPLICATION_ID);
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

    private ReplyProducerRecord<ClientAuthorization> buildReplyProducerRecord(String clientId, String sourceApplicationId) {
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
