package no.novari.flyt.authorization.user

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "novari.flyt.authorization.access-control")
data class AccessControlProperties(
    var permittedAppRoles: Map<String, String> = emptyMap(),
)
