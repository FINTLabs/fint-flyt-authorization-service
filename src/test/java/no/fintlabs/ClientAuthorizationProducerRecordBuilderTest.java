package no.fintlabs;

import no.fintlabs.flyt.authorization.client.ClientAuthorization;
import no.fintlabs.flyt.authorization.client.ClientAuthorizationProducerRecordBuilder;
import no.fintlabs.flyt.authorization.client.sourceapplications.*;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientAuthorizationProducerRecordBuilderTest {

    private ClientAuthorizationProducerRecordBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ClientAuthorizationProducerRecordBuilder();
    }

    @Test
    void testApply_AcosSourceApplicationClientId() {
        String clientId = "acosClientId";
        long sourceAppId = 1L;
        AcosSourceApplication.CLIENT_ID = clientId;

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
        EgrunnervervSourceApplication.CLIENT_ID = clientId;

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
        DigisakSourceApplication.CLIENT_ID = clientId;

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
        VigoSourceApplication.CLIENT_ID = clientId;

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
        AltinnSourceApplication.CLIENT_ID = clientId;

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
        HMSRegSourceApplication.CLIENT_ID = clientId;

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertTrue(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertEquals(sourceAppId, result.getValue().getSourceApplicationId());
    }

    @Test
    void testApply_OtherClientId() {
        String clientId = "otherClientId";
        AcosSourceApplication.CLIENT_ID = null;
        EgrunnervervSourceApplication.CLIENT_ID = null;

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "", clientId);
        ReplyProducerRecord<ClientAuthorization> result = builder.apply(record);

        assertFalse(result.getValue().isAuthorized());
        assertEquals(clientId, result.getValue().getClientId());
        assertNull(result.getValue().getSourceApplicationId());
    }
}
