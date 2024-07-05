package no.fintlabs.flyt.authorization.user.kafka;

import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserPermissionsEntityProducerService {

    private final EntityProducer<User> entityProducer;
    private final EntityTopicNameParameters entityTopicNameParameters;

    public UserPermissionsEntityProducerService(
            EntityProducerFactory entityProducerFactory,
            EntityTopicService entityTopicService
    ) {
        this.entityProducer = entityProducerFactory.createProducer(User.class);
        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .resource("user-permissions")
                .build();
        int retentionTimeInDays = 2;
        long retentionTimeInMilliseconds = TimeUnit.DAYS.toMillis(retentionTimeInDays);
        entityTopicService.ensureTopic(entityTopicNameParameters, retentionTimeInMilliseconds);
    }

    public void send(User user) {
        entityProducer.send(
                EntityProducerRecord.<User>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(String.valueOf(user.getObjectIdentifier()))
                        .value(user)
                        .build()
        );
    }

}
