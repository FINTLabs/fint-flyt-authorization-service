package no.novari.flyt.authorization.user.model

import com.fasterxml.jackson.annotation.JsonProperty
import no.novari.flyt.audit.actor.Actor
import java.time.Instant
import java.util.UUID

data class User(
    val objectIdentifier: UUID,
    val email: String? = null,
    val name: String? = null,
    val sourceApplicationIds: List<Long> = emptyList(),
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdAt: Instant? = null,
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdBy: String? = null,
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdByActor: Actor? = null,
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val lastModifiedAt: Instant? = null,
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val lastModifiedBy: String? = null,
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val lastModifiedByActor: Actor? = null,
)
