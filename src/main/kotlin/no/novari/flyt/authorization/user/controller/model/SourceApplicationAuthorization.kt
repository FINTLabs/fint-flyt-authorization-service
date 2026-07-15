package no.novari.flyt.authorization.user.controller.model

import java.util.UUID

data class SourceApplicationAuthorizationRequest(
    val objectIdentifier: UUID,
    val sourceApplicationIds: Set<Long>,
)

data class SourceApplicationAuthorizationResponse(
    val authorizedSourceApplicationIds: Set<Long>,
)
