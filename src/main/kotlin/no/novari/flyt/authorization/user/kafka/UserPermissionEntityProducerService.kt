package no.novari.flyt.authorization.user.kafka

import no.novari.kafka.starter.producer.send
import no.novari.kafka.starter.topic.EntityCleanupFrequency
import no.novari.kafka.starter.topic.EntityTopicConfig
import no.novari.kafka.starter.topic.EntityTopicName
import no.novari.kafka.starter.topic.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Configuration
class UserPermissionTopicConfiguration {
    @Bean
    fun userPermissionTopic(topics: KafkaTopics): EntityTopicName = topics.entity("userpermission")

    @Bean
    fun userPermissionTopicCreation(
        topics: KafkaTopics,
        userPermissionTopic: EntityTopicName,
    ): NewTopic =
        topics.newTopic(
            userPermissionTopic,
            EntityTopicConfig(
                nullValueRetention = RETENTION_TIME,
                lastValueRetention = RETENTION_TIME,
                cleanupFrequency = EntityCleanupFrequency.FREQUENT,
            ),
        )

    private companion object {
        val RETENTION_TIME: Duration = Duration.ofDays(4)
    }
}

@Service
class UserPermissionEntityProducerService(
    private val kafkaTemplate: KafkaTemplate<String, UserPermission>,
    private val userPermissionTopic: EntityTopicName,
) {
    fun send(userPermission: UserPermission) {
        kafkaTemplate.send(userPermissionTopic, userPermission.objectIdentifier.toString(), userPermission)
    }
}
