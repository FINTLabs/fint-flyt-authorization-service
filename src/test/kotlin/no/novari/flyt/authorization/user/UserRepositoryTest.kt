package no.novari.flyt.authorization.user

import no.novari.flyt.authorization.user.model.UserEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.UUID

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=validate", "spring.datasource.hikari.schema=public"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingTestConfig::class)
class UserRepositoryTest
    @Autowired
    constructor(
        private val userRepository: UserRepository,
    ) {
        @Test
        fun `findSourceApplicationIds gives the user's IDs, and none for an unknown user`() {
            val known = UUID.randomUUID()
            userRepository.saveAndFlush(
                UserEntity(objectIdentifier = known, name = "Known", sourceApplicationIds = mutableListOf(3L, 1L)),
            )

            assertEquals(setOf(1L, 3L), userRepository.findSourceApplicationIds(known))
            assertEquals(emptySet<Long>(), userRepository.findSourceApplicationIds(UUID.randomUUID()))
        }

        companion object {
            @Container
            @ServiceConnection
            @JvmStatic
            val postgres = PostgreSQLContainer("postgres:17-alpine")
        }
    }
