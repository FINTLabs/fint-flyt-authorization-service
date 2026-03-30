package no.novari.flyt.authorization.client

data class ClientAuthorization(
    val authorized: Boolean,
    val clientId: String,
    val sourceApplicationId: Long? = null,
)
