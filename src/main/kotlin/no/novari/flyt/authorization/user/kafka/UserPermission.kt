package no.novari.flyt.authorization.user.kafka

import java.util.UUID

data class UserPermission(
    val objectIdentifier: UUID,
    val sourceApplicationIds: List<Long>,
)
