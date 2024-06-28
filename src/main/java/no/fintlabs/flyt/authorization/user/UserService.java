package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class UserService {

    // TODO eivindmorch 27/06/2024 : Azure sync og tjeneste som leverer til frontend burde være separate
    //  ettersom horisontal skalering av synkingen vil skape problemer

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateUsers(Collection<User> users) {
        log.info("Updating {} user entities", users.size());

        Map<UUID, User> usersToUpdatePerObjectIdentifier = users.stream()
                .collect(toMap(
                        User::getObjectIdentifier,
                        Function.identity()
                ));

        userRepository.deleteByObjectIdentifierNotIn(usersToUpdatePerObjectIdentifier.keySet());

        Map<UUID, UserEntity> updatedUserEntitiesPerObjectIdentifier =
                userRepository.findAllByObjectIdentifierIn(usersToUpdatePerObjectIdentifier.keySet()).stream()
                        .peek(userEntity -> {
                            User newUser = usersToUpdatePerObjectIdentifier.get(userEntity.getObjectIdentifier());
                            userEntity.setName(newUser.getName());
                            userEntity.setEmail(newUser.getEmail());
                        })
                        .collect(toMap(
                                UserEntity::getObjectIdentifier,
                                Function.identity()
                        ));

        Set<UUID> objectIdentifiersForUsersNotExisting = usersToUpdatePerObjectIdentifier.keySet();
        objectIdentifiersForUsersNotExisting.removeAll(updatedUserEntitiesPerObjectIdentifier.keySet());

        List<UserEntity> newUserEntities = objectIdentifiersForUsersNotExisting.stream()
                .map(usersToUpdatePerObjectIdentifier::get)
                .map(user -> UserEntity
                        .builder()
                        .objectIdentifier(user.getObjectIdentifier())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build()
                )
                .toList();

        userRepository.saveAll(
                Stream.concat(
                        updatedUserEntitiesPerObjectIdentifier.values().stream(),
                        newUserEntities.stream()
                ).toList()
        );

        log.info("Successfully updated user entities");
    }

    public Optional<User> find(UUID objectIdentifier) {
        return this.userRepository.findByObjectIdentifier(objectIdentifier)
                .map(this::mapFromEntity);
    }

    public Page<User> getAll(Pageable pageable) {
        return this.userRepository.findAll(pageable)
                .map(this::mapFromEntity);
    }

    public void putAll(List<User> users) {
        Map<UUID, List<Long>> sourceApplicationIdsPerObjectIdentifier = users.stream()
                .collect(toMap(
                        User::getObjectIdentifier,
                        User::getSourceApplicationIds
                ));

        List<UserEntity> entities = userRepository.findAllByObjectIdentifierIn(
                users
                        .stream()
                        .map(User::getObjectIdentifier)
                        .toList()
        );

        entities.forEach(entity -> entity.setSourceApplicationIds(
                sourceApplicationIdsPerObjectIdentifier.get(entity.getObjectIdentifier())
        ));

        userRepository.saveAll(entities);
    }

    private User mapFromEntity(UserEntity userEntity) {
        return User
                .builder()
                .objectIdentifier(userEntity.getObjectIdentifier())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .sourceApplicationIds(userEntity.getSourceApplicationIds())
                .build();
    }

}
