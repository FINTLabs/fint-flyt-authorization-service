package no.novari.flyt.authorization.client.sourceapplications.model

abstract class BaseSourceApplication(
    final override val id: Long,
    final override val displayName: String,
    final override val clientId: String?,
    final override val available: Boolean,
) : SourceApplication
