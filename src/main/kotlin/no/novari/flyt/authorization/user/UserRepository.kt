package no.novari.flyt.authorization.user

import no.novari.flyt.authorization.user.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByObjectIdentifier(sub: UUID): UserEntity?

    fun findAllByObjectIdentifierIn(objectIdentifiers: Collection<UUID>): List<UserEntity>

    @Query(
        """
        SELECT sourceApplicationId
        FROM UserEntity user
        JOIN user.sourceApplicationIds sourceApplicationId
        WHERE user.objectIdentifier = :objectIdentifier
          AND sourceApplicationId IN :sourceApplicationIds
        """,
    )
    fun findAuthorizedSourceApplicationIds(
        @Param("objectIdentifier") objectIdentifier: UUID,
        @Param("sourceApplicationIds") sourceApplicationIds: Set<Long>,
    ): Set<Long>
}
