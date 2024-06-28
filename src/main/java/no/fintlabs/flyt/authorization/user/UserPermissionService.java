package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.model.UserPermission;
import no.fintlabs.flyt.authorization.user.model.UserPermissionEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class UserPermissionService {

    // TODO eivindmorch 27/06/2024 : Azure sync og tjeneste som leverer til frontend burde være separate
    //  ettersom horisontal skalering av synkingen vil skape problemer

    private final UserPermissionRepository userPermissionRepository;

    public UserPermissionService(UserPermissionRepository userPermissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
    }

    public void updateUserPermissions(Set<UUID> permittedUsersObjectIdentifiers) {
        log.info("Updating user permissions");

        userPermissionRepository.deleteByObjectIdentifierNotIn(permittedUsersObjectIdentifiers); // TODO eivindmorch 27/06/2024 : Soft delete?

        Set<UUID> objectIdentifiersAlreadyPersisted =
                userPermissionRepository.findObjectIdentifiersByObjectIdentifierIn(permittedUsersObjectIdentifiers)
                        .stream()
                        .map(UserPermissionRepository.ObjectIdentifierSelection::getObjectIdentifier)
                        .collect(Collectors.toSet());
        Set<UUID> objectIdentifiersNotAlreadyPersisted = new HashSet<>(permittedUsersObjectIdentifiers);
        objectIdentifiersNotAlreadyPersisted.removeAll(objectIdentifiersAlreadyPersisted);
        userPermissionRepository.saveAll(
                objectIdentifiersNotAlreadyPersisted.stream()
                        .map(objectIdentifier -> UserPermissionEntity
                                .builder()
                                .objectIdentifier(objectIdentifier)
                                .build())
                        .toList()
        );
        log.info("Successfully updated user permissions");
    }

    public Optional<UserPermission> find(UUID objectIdentifier) {
        return this.userPermissionRepository.findByObjectIdentifier(objectIdentifier)
                .map(this::mapFromEntity);
    }

    public List<UserPermission> getAll() {
        return this.userPermissionRepository.findAll()
                .stream()
                .map(this::mapFromEntity)
                .toList();
    }

    public List<UserPermission> putAll(List<UserPermission> userPermissions) {
        Map<UUID, List<Long>> sourceApplicationIdsPerObjectIdentifier = userPermissions.stream()
                .collect(toMap(
                        UserPermission::getObjectIdentifier,
                        UserPermission::getSourceApplicationIds
                ));

        List<UserPermissionEntity> entities = userPermissionRepository.findAllByObjectIdentifierIn(
                userPermissions
                        .stream()
                        .map(UserPermission::getObjectIdentifier)
                        .toList()
        );

        entities.forEach(entity -> entity.setSourceApplicationIds(
                sourceApplicationIdsPerObjectIdentifier.get(entity.getObjectIdentifier())
        ));

        return userPermissionRepository.saveAll(entities).stream()
                .map(this::mapFromEntity)
                .collect(Collectors.toList());
    }

    private UserPermission mapFromEntity(UserPermissionEntity userPermissionEntity) {
        return UserPermission
                .builder()
                .objectIdentifier(userPermissionEntity.getObjectIdentifier())
                .sourceApplicationIds(userPermissionEntity.getSourceApplicationIds())
                .build();
    }

}
