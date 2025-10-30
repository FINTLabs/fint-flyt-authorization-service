package no.fintlabs.flyt.authorization.user.kafka;

import no.fintlabs.kafka.model.ParameterizedProducerRecord;
import no.fintlabs.kafka.producing.ParameterizedTemplate;
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory;
import no.fintlabs.kafka.topic.EntityTopicService;
import no.fintlabs.kafka.topic.configuration.EntityCleanupFrequency;
import no.fintlabs.kafka.topic.configuration.EntityTopicConfiguration;
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UserPermissionEntityProducerService {

    private final ParameterizedTemplate<UserPermission> parameterizedTemplate;
    private final EntityTopicNameParameters entityTopicNameParameters;

    private static final Duration RETENTION_TIME = Duration.ofDays(1);

    public UserPermissionEntityProducerService(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            EntityTopicService entityTopicService
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(UserPermission.class);

        entityTopicNameParameters = EntityTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build())
                .resourceName("userpermission")
                .build();

        entityTopicService.createOrModifyTopic(entityTopicNameParameters, EntityTopicConfiguration.builder()
                .partitions(1)
                .lastValueRetentionTime(RETENTION_TIME)
                .nullValueRetentionTime(RETENTION_TIME)
                .cleanupFrequency(EntityCleanupFrequency.FREQUENT)
                .build()
        );
    }

    public void send(UserPermission userPermission) {
        parameterizedTemplate.send(
                ParameterizedProducerRecord.<UserPermission>builder()
                        .topicNameParameters(entityTopicNameParameters)
                        .key(String.valueOf(userPermission.getObjectIdentifier()))
                        .value(userPermission)
                        .build()
        );
    }

}
