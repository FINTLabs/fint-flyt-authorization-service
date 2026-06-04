package no.novari.flyt.authorization.user

import no.novari.flyt.authorization.user.kafka.UserPermission
import no.novari.flyt.authorization.user.kafka.UserPermissionEntityProducerService
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.authorization.user.model.UserEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPermissionEntityProducerService: UserPermissionEntityProducerService,
) {
    fun publishUsers() {
        logger.info("Starting publishing users")
        try {
            val userPermissions =
                userRepository
                    .findAll()
                    .map(::mapFromEntityToUserPermission)

            logger.info("Retrieved and mapped {} user entities", userPermissions.size)
            userPermissions.forEach(userPermissionEntityProducerService::send)
            logger.info("Successfully published users")
        } catch (exception: Exception) {
            logger.error("Error while publishing users", exception)
        }
    }

    fun findOrCreate(user: User): User {
        userRepository.findByObjectIdentifier(user.objectIdentifier)?.let {
            return mapFromEntity(it)
        }

        return try {
            val savedEntity = userRepository.saveAndFlush(mapFromUser(user))
            userPermissionEntityProducerService.send(mapFromEntityToUserPermission(savedEntity))
            mapFromEntity(savedEntity)
        } catch (exception: DataIntegrityViolationException) {
            val existing = userRepository.findByObjectIdentifier(user.objectIdentifier) ?: throw exception
            logger.info(
                "Lost race when creating user with objectIdentifier={}; returning existing user",
                user.objectIdentifier,
            )
            mapFromEntity(existing)
        }
    }

    fun getAll(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable).map(::mapFromEntity)
    }

    fun putAll(users: List<User>) {
        val sourceApplicationIdsPerObjectIdentifier =
            users.associate { user ->
                user.objectIdentifier to user.sourceApplicationIds
            }

        val entities = userRepository.findAllByObjectIdentifierIn(users.map(User::objectIdentifier))

        entities.forEach { entity ->
            entity.sourceApplicationIds =
                sourceApplicationIdsPerObjectIdentifier[checkNotNull(entity.objectIdentifier)]
                    ?.toMutableList()
                    ?: mutableListOf()
        }

        userRepository.saveAll(entities)
    }

    private fun mapFromUser(user: User): UserEntity {
        return UserEntity(
            objectIdentifier = user.objectIdentifier,
            name = user.name,
            email = user.email,
            sourceApplicationIds = user.sourceApplicationIds.toMutableList(),
        )
    }

    private fun mapFromEntity(userEntity: UserEntity): User {
        return User(
            objectIdentifier = checkNotNull(userEntity.objectIdentifier),
            name = userEntity.name,
            email = userEntity.email,
            sourceApplicationIds = userEntity.sourceApplicationIds.toList(),
        )
    }

    private fun mapFromEntityToUserPermission(userEntity: UserEntity): UserPermission {
        return UserPermission(
            objectIdentifier = checkNotNull(userEntity.objectIdentifier),
            sourceApplicationIds = userEntity.sourceApplicationIds.toList(),
        )
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
