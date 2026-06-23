package no.novari.flyt.authorization.user

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

/**
 * Verifiserer at V3 konverterer en eksisterende `revinfo.actor` (varchar) med blandet innhold —
 * e-poster (med og uten treff i user_entity), UUID-strenger, "system" og ukjente strenger — til
 * korrekt JSONB Actor. Flyway kjøres først til V2, seedes med realistiske rader, deretter til V3.
 */
@Testcontainers(disabledWithoutDocker = true)
class RevinfoActorMigrationTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setUp() {
        migrate(target = "2")
        connection = DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
        Flyway
            .configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .cleanDisabled(false)
            .load()
            .clean()
    }

    @Test
    fun `converts mixed actor strings to typed JSONB actors`() {
        val oidAda = UUID.randomUUID()
        val oidDup1 = UUID.randomUUID()
        val oidDup2 = UUID.randomUUID()
        val standaloneOid = UUID.randomUUID()
        val uppercaseOid = UUID.randomUUID()

        insertUser(oidAda, "ada@example.no")
        insertUser(oidDup1, "dup@example.no")
        insertUser(oidDup2, "dup@example.no")

        insertRevision(1, "ada@example.no")
        insertRevision(2, "dup@example.no")
        insertRevision(3, "ghost@nowhere.no")
        insertRevision(4, standaloneOid.toString())
        insertRevision(5, "system")
        insertRevision(6, "")
        insertRevision(7, uppercaseOid.toString().uppercase())
        insertUserEntityAudRow(rev = 1, userId = 1L)

        migrate(target = "3")

        assertActor(rev = 1, expectedType = "USER", expectedOid = oidAda.toString())
        assertActor(rev = 2, expectedType = "UNKNOWN", expectedOid = null)
        assertActor(rev = 3, expectedType = "UNKNOWN", expectedOid = null)
        assertActor(rev = 4, expectedType = "USER", expectedOid = standaloneOid.toString())
        assertActor(rev = 5, expectedType = "SYSTEM", expectedOid = null)
        assertActor(rev = 6, expectedType = "SYSTEM", expectedOid = null)
        assertActor(rev = 7, expectedType = "USER", expectedOid = uppercaseOid.toString().uppercase())

        assertEquals(1L, countUserEntityAudRows(), "Envers _aud-rader skal overleve revinfo-migreringen")
        assertTrue(nextRevSequenceValue() > 7, "revinfo_seq skal starte trygt over eksisterende rev")
    }

    private fun insertUser(
        objectIdentifier: UUID,
        email: String,
    ) {
        connection
            .prepareStatement(
                "insert into user_entity (email, name, object_identifier) values (?, ?, ?)",
            ).use { statement ->
                statement.setString(1, email)
                statement.setString(2, email.substringBefore('@'))
                statement.setObject(3, objectIdentifier)
                statement.executeUpdate()
            }
    }

    private fun insertRevision(
        rev: Int,
        actor: String,
    ) {
        connection
            .prepareStatement("insert into revinfo (rev, revtstmp, actor) values (?, ?, ?)")
            .use { statement ->
                statement.setInt(1, rev)
                statement.setLong(2, 1_000L + rev)
                statement.setString(3, actor)
                statement.executeUpdate()
            }
    }

    private fun insertUserEntityAudRow(
        rev: Int,
        userId: Long,
    ) {
        connection
            .prepareStatement("insert into user_entity_aud (rev, revtype, id, object_identifier) values (?, 0, ?, ?)")
            .use { statement ->
                statement.setInt(1, rev)
                statement.setLong(2, userId)
                statement.setObject(3, UUID.randomUUID())
                statement.executeUpdate()
            }
    }

    private fun assertActor(
        rev: Int,
        expectedType: String,
        expectedOid: String?,
    ) {
        connection
            .prepareStatement("select actor->>'type' as type, actor->>'oid' as oid from revinfo where rev = ?")
            .use { statement ->
                statement.setLong(1, rev.toLong())
                statement.executeQuery().use { resultSet ->
                    assertTrue(resultSet.next(), "Fant ingen revinfo-rad for rev=$rev")
                    assertEquals(expectedType, resultSet.getString("type"), "Feil actor-type for rev=$rev")
                    if (expectedOid == null) {
                        assertNull(resultSet.getString("oid"), "Forventet ingen oid for rev=$rev")
                    } else {
                        assertEquals(expectedOid, resultSet.getString("oid"), "Feil oid for rev=$rev")
                    }
                }
            }
    }

    private fun countUserEntityAudRows(): Long =
        connection.prepareStatement("select count(*) from user_entity_aud").use { statement ->
            statement.executeQuery().use { resultSet ->
                resultSet.next()
                resultSet.getLong(1)
            }
        }

    private fun nextRevSequenceValue(): Long =
        connection.prepareStatement("select nextval('revinfo_seq')").use { statement ->
            statement.executeQuery().use { resultSet ->
                resultSet.next()
                resultSet.getLong(1)
            }
        }

    private companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine")

        fun migrate(target: String) {
            Flyway
                .configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .target(target)
                .load()
                .migrate()
        }
    }
}
