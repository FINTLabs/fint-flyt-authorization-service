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
    fun `find authorized source application IDs queries only requested IDs`() {
        val objectIdentifier = UUID.randomUUID()
        val repository =
            mock<UserRepository> {
                on { findAuthorizedSourceApplicationIds(objectIdentifier, setOf(1L, 2L, 3L)) } doReturn setOf(2L, 3L)
            }
        val service = UserService(repository, mock(), actorDisplayResolver)

        val result = service.findAuthorizedSourceApplicationIds(objectIdentifier, setOf(1L, 2L, 3L))

        assertEquals(setOf(2L, 3L), result)
        verify(repository).findAuthorizedSourceApplicationIds(objectIdentifier, setOf(1L, 2L, 3L))
        verify(repository, never()).findByObjectIdentifier(any())
    }

    @Test
    fun `find authorized source application IDs skips repository for empty input`() {
        val repository = mock<UserRepository>()
        val service = UserService(repository, mock(), actorDisplayResolver)

        assertEquals(emptySet<Long>(), service.findAuthorizedSourceApplicationIds(UUID.randomUUID(), emptySet()))
        verify(repository, never()).findAuthorizedSourceApplicationIds(any(), any())
    }

    @Test
    fun `findOrCreate does not save when token name and email match stored values`() {
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
                    name = "Existing",
                    email = "existing@novari.no",
                    sourceApplicationIds = listOf(1L),
                ),
            )

        assertEquals(objectIdentifier, result.objectIdentifier)
        assertEquals("Existing", result.name)
        verify(repository, never()).save(any<UserEntity>())
        verify(repository, never()).saveAndFlush(any<UserEntity>())
        verify(producer, never()).send(any())
    }

    @Test
    fun `findOrCreate updates name and email when they differ from stored values`() {
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
                on { save(any<UserEntity>()) } doAnswer { it.arguments[0] as UserEntity }
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

        assertEquals("From token", result.name)
        assertEquals("token@novari.no", result.email)
        verify(repository).save(existing)
        verify(producer, never()).send(any())
    }

    @Test
    fun `findOrCreate preserves stored name when token name is blank`() {
        val objectIdentifier = UUID.randomUUID()
        val existing =
            UserEntity(
                objectIdentifier = objectIdentifier,
                name = "Manually corrected name",
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
                    name = "",
                    email = "existing@novari.no",
                    sourceApplicationIds = listOf(1L),
                ),
            )

        assertEquals("Manually corrected name", result.name)
        verify(repository, never()).save(any<UserEntity>())
        verify(producer, never()).send(any())
    }

    @Test
    fun `findOrCreate updates email while preserving blank token name`() {
        val objectIdentifier = UUID.randomUUID()
        val existing =
            UserEntity(
                objectIdentifier = objectIdentifier,
                name = "Manually corrected name",
                email = "old@novari.no",
                sourceApplicationIds = mutableListOf(1L),
            ).apply { id = 1L }

        val repository =
            mock<UserRepository> {
                on { findByObjectIdentifier(objectIdentifier) } doReturn existing
                on { save(any<UserEntity>()) } doAnswer { it.arguments[0] as UserEntity }
            }
        val producer = mock<UserPermissionEntityProducerService>()
        val service = UserService(repository, producer, actorDisplayResolver)

        val result =
            service.findOrCreate(
                User(
                    objectIdentifier = objectIdentifier,
                    name = null,
                    email = "new@novari.no",
                    sourceApplicationIds = listOf(1L),
                ),
            )

        assertEquals("Manually corrected name", result.name)
        assertEquals("new@novari.no", result.email)
        verify(repository).save(existing)
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
