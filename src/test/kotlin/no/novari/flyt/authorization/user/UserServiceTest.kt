package no.novari.flyt.authorization.user

import no.novari.flyt.audit.actor.ActorDisplayProperties
import no.novari.flyt.audit.actor.ActorDisplayResolver
import no.novari.flyt.audit.actor.ActorNameLookup
import no.novari.flyt.authorization.user.kafka.UserPermission
import no.novari.flyt.authorization.user.kafka.UserPermissionEntityProducerService
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.authorization.user.model.UserEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.dao.DataIntegrityViolationException
import java.util.UUID

class UserServiceTest {
    private val actorDisplayResolver = ActorDisplayResolver(ActorNameLookup { emptyMap() }, ActorDisplayProperties())

    @Test
    fun `findOrCreate returns existing user without inserting or publishing`() {
        val objectIdentifier = UUID.randomUUID()
        val existing =
            UserEntity(
                objectIdentifier = objectIdentifier,
                name = "Existing",
                email = "existing@novari.no",
                sourceApplicationIds = mutableListOf(1L),
            ).apply { id = 1L }

        val repository =
            mock<UserRepository> {
                on { findByObjectIdentifier(objectIdentifier) } doReturn existing
            }
        val producer = mock<UserPermissionEntityProducerService>()
        val service = UserService(repository, producer, actorDisplayResolver)

        val result =
            service.findOrCreate(
                User(
                    objectIdentifier = objectIdentifier,
                    name = "From token",
                    email = "token@novari.no",
                    sourceApplicationIds = listOf(1L),
                ),
            )

        assertEquals(objectIdentifier, result.objectIdentifier)
        assertEquals("Existing", result.name)
        verify(repository, never()).saveAndFlush(any<UserEntity>())
        verify(producer, never()).send(any())
    }

    @Test
    fun `findOrCreate inserts and publishes when user is missing`() {
        val objectIdentifier = UUID.randomUUID()
        val repository =
            mock<UserRepository> {
                on { findByObjectIdentifier(objectIdentifier) } doReturn null
                on { saveAndFlush(any<UserEntity>()) } doAnswer { invocation ->
                    (invocation.arguments[0] as UserEntity).apply { id = 7L }
                }
            }
        val producer = mock<UserPermissionEntityProducerService>()
        val service = UserService(repository, producer, actorDisplayResolver)

        val result =
            service.findOrCreate(
                User(
                    objectIdentifier = objectIdentifier,
                    name = "New user",
                    email = "new@novari.no",
                    sourceApplicationIds = listOf(2L, 3L),
                ),
            )

        assertEquals(objectIdentifier, result.objectIdentifier)
        assertEquals("New user", result.name)
        assertEquals(listOf(2L, 3L), result.sourceApplicationIds)
        verify(repository).saveAndFlush(any<UserEntity>())
        verify(producer).send(UserPermission(objectIdentifier, listOf(2L, 3L)))
    }

    @Test
    fun `findOrCreate returns winning row when insert hits unique constraint after losing race`() {
        val objectIdentifier = UUID.randomUUID()
        val winningRow =
            UserEntity(
                objectIdentifier = objectIdentifier,
                name = "Winner",
                email = "winner@novari.no",
                sourceApplicationIds = mutableListOf(5L),
            ).apply { id = 99L }

        val repository =
            mock<UserRepository> {
                on { findByObjectIdentifier(objectIdentifier) } doReturn null doReturn winningRow
                on {
                    saveAndFlush(any<UserEntity>())
                } doThrow DataIntegrityViolationException("uk_td2dvdf4t2le4cydfk7a1x17i")
            }
        val producer = mock<UserPermissionEntityProducerService>()
        val service = UserService(repository, producer, actorDisplayResolver)

        val result =
            service.findOrCreate(
                User(
                    objectIdentifier = objectIdentifier,
                    name = "Loser",
                    email = "loser@novari.no",
                    sourceApplicationIds = listOf(1L),
                ),
            )

        assertEquals(objectIdentifier, result.objectIdentifier)
        assertEquals("Winner", result.name)
        assertEquals(listOf(5L), result.sourceApplicationIds)
        verify(repository, times(2)).findByObjectIdentifier(objectIdentifier)
        verify(producer, never()).send(any())
    }

    @Test
    fun `findOrCreate rethrows when insert fails and refetch still finds nothing`() {
        val objectIdentifier = UUID.randomUUID()
        val repository =
            mock<UserRepository> {
                on { findByObjectIdentifier(objectIdentifier) } doReturn null
                on {
                    saveAndFlush(any<UserEntity>())
                } doThrow DataIntegrityViolationException("some other constraint")
            }
        val producer = mock<UserPermissionEntityProducerService>()
        val service = UserService(repository, producer, actorDisplayResolver)

        assertThrows(DataIntegrityViolationException::class.java) {
            service.findOrCreate(
                User(
                    objectIdentifier = objectIdentifier,
                    name = "X",
                    email = "x@novari.no",
                    sourceApplicationIds = listOf(1L),
                ),
            )
        }
        verify(producer, never()).send(any())
    }
}
