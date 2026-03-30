package no.novari.flyt.authorization.user

import no.novari.flyt.authorization.user.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByObjectIdentifier(sub: UUID): UserEntity?

    fun findAllByObjectIdentifierIn(objectIdentifiers: Collection<UUID>): List<UserEntity>
}
