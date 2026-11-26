package no.novari.flyt.authorization.user

import no.novari.flyt.audit.actor.Actor
import no.novari.flyt.audit.actor.ActorAuditorAware
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@TestConfiguration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "flytAuditorAware")
class JpaAuditingTestConfig {
    @Bean
    fun flytAuditorAware(): AuditorAware<Actor> = ActorAuditorAware()
}
