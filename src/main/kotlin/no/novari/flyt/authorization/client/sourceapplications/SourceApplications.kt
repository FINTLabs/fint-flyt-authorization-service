package no.novari.flyt.authorization.client.sourceapplications

import no.novari.flyt.security.SourceApplication
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/** A [SourceApplication] with this namespace's client ID and availability, from `fint.flyt.<app>.*`. */
data class ConfiguredSourceApplication(
    val sourceApplication: SourceApplication,
    val clientId: String?,
    val available: Boolean,
) {
    val id: Long get() = sourceApplication.id
    val displayName: String get() = sourceApplication.displayName
}

@Component
class SourceApplications(
    environment: Environment,
) {
    val all: List<ConfiguredSourceApplication> =
        SourceApplication.entries.map {
            ConfiguredSourceApplication(
                sourceApplication = it,
                clientId = environment.getProperty(it.clientIdProperty),
                available = environment.getProperty("${it.propertyPrefix}.available", Boolean::class.java, false),
            )
        }
}
