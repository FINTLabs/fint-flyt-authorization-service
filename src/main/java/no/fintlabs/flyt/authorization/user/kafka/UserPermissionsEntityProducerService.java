package no.fintlabs.flyt.authorization.user.kafka;

import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserPermissionsEntityProducerService {

    private final EntityProducer<UserPermission> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public UserPermissionsEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService
    ) {
        this.entityProducer = entityProducerFactory.createProducer(UserPermission.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("user-permissions")
                .build();
        int retentionTimeInHours = 1;
        long retentionTimeInMilliseconds = TimeUnit.HOURS.toMillis(retentionTimeInHours);
        entityTopicService.ensureTopic(entityTopicNameParameters, retentionTimeInMilliseconds);
    }

    public void send(UserPermission userPermission) {
        entityProducer.send(
                EntityProducerRecord.<UserPermission>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(String.valueOf(userPermission.getObjectIdentifier()))
                        .value(userPermission)
                        .build()
        );
    }

}
