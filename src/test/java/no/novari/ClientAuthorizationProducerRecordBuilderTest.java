package no.novari;

import no.novari.flyt.authorization.client.ClientAuthorization;
import no.novari.flyt.authorization.client.ClientAuthorizationProducerRecordBuilder;
import no.novari.flyt.authorization.client.sourceapplications.model.*;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientAuthorizationProducerRecordBuilderTest {

    @Test
    void testApply_AcosSourceApplicationClientId() {
        String clientId = "acosClientId";
        long sourceAppId = 1L;
        ClientAuthorizationProducerRecordBuilder builder = builderFor(new AcosSourceApplication(clientId));

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_EgrunnervervSourceApplicationClientId() {
        String clientId = "egrunnervervClientId";
        long sourceAppId = 2L;
        ClientAuthorizationProducerRecordBuilder builder = builderFor(new EgrunnervervSourceApplication(clientId));

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_DigisakSourceApplicationClientId() {
        String clientId = "digisakClientId";
        long sourceAppId = 3L;
        ClientAuthorizationProducerRecordBuilder builder = builderFor(new DigisakSourceApplication(clientId));

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_VigoSourceApplicationClientId() {
        String clientId = "vigoClientId";
        long sourceAppId = 4L;
        ClientAuthorizationProducerRecordBuilder builder = builderFor(new VigoSourceApplication(clientId));

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_AltinnSourceApplicationClientId() {
        String clientId = "altinnClientId";
        long sourceAppId = 5L;
        ClientAuthorizationProducerRecordBuilder builder = builderFor(new AltinnSourceApplication(clientId));

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_HMSregSourceApplicationClientId() {
        String clientId = "hmsregClientId";
        long sourceAppId = 6L;
        ClientAuthorizationProducerRecordBuilder builder = builderFor(new HMSRegSourceApplication(clientId));

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_OtherClientId() {
        String clientId = "otherClientId";
        ClientAuthorizationProducerRecordBuilder builder = builderFor(
                new AcosSourceApplication(null),
                new EgrunnervervSourceApplication(null)
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertFalse(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertNull(result.getValue().getSourceApplicationId());
    }

    private ClientAuthorizationProducerRecordBuilder builderFor(SourceApplication... sourceApplications) {
        return new ClientAuthorizationProducerRecordBuilder(List.of(sourceApplications));
    }
}
