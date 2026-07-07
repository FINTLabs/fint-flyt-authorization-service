package no.novari.flyt.authorization.user.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.UniqueConstraint
import no.novari.flyt.audit.entity.AuditedEntity
import org.hibernate.annotations.NaturalId
import org.hibernate.envers.AuditJoinTable
import org.hibernate.envers.AuditTable
import org.hibernate.envers.Audited
import java.util.UUID

@Entity
@Audited
@AuditTable("user_entity_aud")
class UserEntity(
    @NaturalId
    @Column(nullable = false, unique = true)
    var objectIdentifier: UUID? = null,
    var email: String? = null,
    var name: String? = null,
    @Audited
    @AuditJoinTable(name = "user_entity_source_application_ids_aud")
    @ElementCollection
    @CollectionTable(
        name = "user_entity_source_application_ids",
        joinColumns = [JoinColumn(name = "user_entity_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["user_entity_id", "source_application_ids"])],
    )
    @Column(name = "source_application_ids")
    var sourceApplicationIds: MutableList<Long> = mutableListOf(),
) : AuditedEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    var id: Long? = null
}
