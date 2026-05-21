package no.novari.flyt.authorization.user.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp

@Entity
@Table(name = "revinfo")
@RevisionEntity(SecurityRevisionListener::class)
class RevisionInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev")
    var id: Int? = null,
    @RevisionTimestamp
    @Column(name = "revtstmp", nullable = false)
    var timestamp: Long? = null,
    @Column(name = "actor", nullable = false)
    var actor: String = SYSTEM_ACTOR,
) {
    companion object {
        const val SYSTEM_ACTOR: String = "system"
    }
}
