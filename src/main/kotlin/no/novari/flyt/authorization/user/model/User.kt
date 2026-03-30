package no.novari.flyt.authorization.user.model

import java.util.UUID

data class User(
    val objectIdentifier: UUID,
    val email: String? = null,
    val name: String? = null,
    val sourceApplicationIds: List<Long> = emptyList(),
)
