package no.novari.flyt.authorization.user

import jakarta.persistence.EntityManager
import no.novari.flyt.authorization.user.audit.RevisionInfo
import no.novari.flyt.authorization.user.model.UserEntity
import org.hibernate.envers.AuditReaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.transaction.TestTransaction
import java.util.UUID

@DataJpaTest(
    properties = [
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:user-auditing;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ],
)
class UserEntityAuditingTest
    @Autowired constructor(
        private val userRepository: UserRepository,
        private val entityManager: EntityManager,
) {
    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `persists user permission revisions with actor metadata`() {
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("bootstrap@novari.no", null)

        val createdUser =
            userRepository.saveAndFlush(
                UserEntity(
                    objectIdentifier = UUID.randomUUID(),
                    email = "user@example.no",
                    name = "Test User",
                    sourceApplicationIds = mutableListOf(1L),
                ),
            )

        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()

        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("admin@novari.no", null)

        val updatedUser = userRepository.findById(checkNotNull(createdUser.id)).orElseThrow()
        updatedUser.sourceApplicationIds = mutableListOf(1L, 2L)
        userRepository.saveAndFlush(updatedUser)

        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()
        entityManager.clear()

        val auditReader = AuditReaderFactory.get(entityManager)
        val revisions = auditReader.getRevisions(UserEntity::class.java, createdUser.id)

        assertEquals(listOf(1, 2), revisions)
        assertEquals(listOf(1L), auditReader.find(UserEntity::class.java, createdUser.id, 1).sourceApplicationIds)
        assertEquals(listOf(1L, 2L), auditReader.find(UserEntity::class.java, createdUser.id, 2).sourceApplicationIds)
        assertEquals("bootstrap@novari.no", auditReader.findRevision(RevisionInfo::class.java, 1).actor)
        assertEquals("admin@novari.no", auditReader.findRevision(RevisionInfo::class.java, 2).actor)
    }
}
