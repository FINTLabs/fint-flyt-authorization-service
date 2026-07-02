package no.novari.flyt.authorization.config

import no.novari.flyt.audit.actor.ActorNameLookup
import no.novari.flyt.authorization.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Overstyrer flyt-audit-starters default [ActorNameLookup] (HTTP mot seg selv) med et
 * lokalt oppslag i egen [UserRepository]. Denne tjenesten ER `fint-flyt-authorization-service`
 * — den skal aldri kalle seg selv over nettverk for å hente brukernavn.
 */
@Configuration
class LocalActorNameLookupConfig {
    @Bean
    fun localActorNameLookup(userRepository: UserRepository): ActorNameLookup =
        ActorNameLookup { oids ->
            userRepository
                .findAllByObjectIdentifierIn(oids)
                .associate { checkNotNull(it.objectIdentifier) to it.name }
        }
}
