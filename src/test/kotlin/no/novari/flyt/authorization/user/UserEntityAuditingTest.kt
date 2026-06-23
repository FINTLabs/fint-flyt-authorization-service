package no.novari.flyt.authorization.user

import jakarta.persistence.EntityManager
import no.novari.flyt.audit.actor.Actor
import no.novari.flyt.audit.revision.ActorRevisionEntity
import no.novari.flyt.authorization.user.model.UserEntity
import org.hibernate.envers.AuditReaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.transaction.TestTransaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.hikari.schema=public",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingTestConfig::class)
class UserEntityAuditingTest
    @Autowired
    constructor(
        private val userRepository: UserRepository,
        private val entityManager: EntityManager,
    ) {
        @AfterEach
        fun tearDown() {
            SecurityContextHolder.clearContext()
        }

        @Test
        fun `persists user permission revisions with actor metadata`() {
            val creatorOid = UUID.randomUUID()
            val editorOid = UUID.randomUUID()

            authenticateAs(creatorOid)

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

            authenticateAs(editorOid)

            val updatedUser = userRepository.findById(checkNotNull(createdUser.id)).orElseThrow()
            updatedUser.sourceApplicationIds = mutableListOf(1L, 2L)
            userRepository.saveAndFlush(updatedUser)

            TestTransaction.flagForCommit()
            TestTransaction.end()

            TestTransaction.start()
            entityManager.clear()

            val auditReader = AuditReaderFactory.get(entityManager)
            val revisions = auditReader.getRevisions(UserEntity::class.java, createdUser.id)

            assertEquals(listOf(1L, 2L), revisions)
            assertEquals(listOf(1L), auditReader.find(UserEntity::class.java, createdUser.id, 1).sourceApplicationIds)
            assertEquals(
                listOf(1L, 2L),
                auditReader.find(UserEntity::class.java, createdUser.id, 2).sourceApplicationIds,
            )

            assertEquals(
                Actor.User(creatorOid),
                auditReader.findRevision(ActorRevisionEntity::class.java, 1).actor,
            )
            assertEquals(
                Actor.User(editorOid),
                auditReader.findRevision(ActorRevisionEntity::class.java, 2).actor,
            )

            val persistedUser = userRepository.findById(createdUser.id!!).orElseThrow()
            assertEquals(Actor.User(creatorOid), persistedUser.createdBy)
            assertEquals(Actor.User(editorOid), persistedUser.lastModifiedBy)
            assertNotNull(persistedUser.createdAt)
            assertNotNull(persistedUser.lastModifiedAt)
        }

        private fun authenticateAs(oid: UUID) {
            val jwt =
                Jwt
                    .withTokenValue("token")
                    .header("alg", "none")
                    .claim("oid", oid.toString())
                    .build()
            SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
        }

        companion object {
            @Container
            @ServiceConnection
            @JvmStatic
            val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine")
        }
    }
