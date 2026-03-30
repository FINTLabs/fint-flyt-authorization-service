package no.novari.flyt.authorization.user.kafka

import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserPermissionEntityProducerService(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
) {
    private val parameterizedTemplate: ParameterizedTemplate<UserPermission> =
        parameterizedTemplateFactory.createTemplate(UserPermission::class.java)

    private val entityTopicNameParameters: EntityTopicNameParameters =
        EntityTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgIdApplicationDefault()
                    .domainContextApplicationDefault()
                    .build(),
            ).resourceName("userpermission")
            .build()

    init {
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            EntityTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .lastValueRetentionTime(RETENTION_TIME)
                .nullValueRetentionTime(RETENTION_TIME)
                .cleanupFrequency(EntityCleanupFrequency.FREQUENT)
                .build(),
        )
    }

    fun send(userPermission: UserPermission) {
        parameterizedTemplate.send(
            ParameterizedProducerRecord
                .builder<UserPermission>()
                .topicNameParameters(entityTopicNameParameters)
                .key(userPermission.objectIdentifier.toString())
                .value(userPermission)
                .build(),
        )
    }

    private companion object {
        val RETENTION_TIME: Duration = Duration.ofDays(4)
        const val PARTITIONS: Int = 1
    }
}
