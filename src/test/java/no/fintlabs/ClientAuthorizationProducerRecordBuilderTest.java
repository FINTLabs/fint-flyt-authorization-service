package no.fintlabs;

import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.models.sourceapplication.AcosSourceApplication;
import no.fintlabs.models.sourceapplication.EgrunnervervSourceApplication;
import no.fintlabs.models.sourceapplication.DigisakSourceApplication;
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
        String sourceAppId = "1";
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
        String sourceAppId = "2";
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
        String sourceAppId = "3";
        DigisakSourceApplication.CLIENT_ID = clientId;

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
