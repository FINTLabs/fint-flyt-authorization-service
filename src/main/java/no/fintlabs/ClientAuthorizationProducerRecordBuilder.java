package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.models.AcosSourceApplication;
import no.fintlabs.models.EgrunnervervSourceApplication;
import no.fintlabs.models.RF1350SourceApplication;
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
        } else if (RF1350SourceApplication.CLIENT_ID != null && Objects.equals(consumerRecord.value(), RF1350SourceApplication.CLIENT_ID)) {
            log.debug("Request by client with id="+RF1350SourceApplication.CLIENT_ID+". Returning source application id="+RF1350SourceApplication.SOURCE_APPLICATION_ID);
            return buildReplyProducerRecord(RF1350SourceApplication.CLIENT_ID, RF1350SourceApplication.SOURCE_APPLICATION_ID);
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
