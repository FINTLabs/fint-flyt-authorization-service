package no.novari.flyt.authorization.client.sourceapplications.model

interface SourceApplication {
    val id: Long
    val clientId: String?
    val displayName: String
    val available: Boolean
}
