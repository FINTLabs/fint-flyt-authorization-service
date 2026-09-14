package no.novari.flyt.authorization.user.controller.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Request to authorize source applications for a user.")
data class SourceApplicationAuthorizationRequest(
    val objectIdentifier: UUID,
    val sourceApplicationIds: Set<Long>,
)

@Schema(description = "The requested source applications the user is authorized to access.")
data class SourceApplicationAuthorizationResponse(
    val authorizedSourceApplicationIds: Set<Long>,
)
