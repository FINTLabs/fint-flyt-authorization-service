package no.fintlabs.flyt.authorization.userpermission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;

    public UserPermissionService(UserPermissionRepository userPermissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
    }

    public Optional<UserPermission> get(String objectIdentifier) {
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
        List<UserPermissionEntity> existingEntities = userPermissions.stream()
                .map(userPermission -> userPermissionRepository.findByObjectIdentifier(userPermission.getObjectIdentifier())
                        .map(entity -> {
                            entity.setSourceApplicationIds(userPermission.getSourceApplicationIds());
                            return entity;
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<UserPermissionEntity> savedEntities = userPermissionRepository.saveAll(existingEntities);

        return savedEntities.stream()
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

    public void refreshUserPermissions(List<UserPermissionEntity> userPermissionEntities) {

        deleteUserPermissionsNotInList(userPermissionEntities);

        List<UserPermissionEntity> newUserPermissionEntityList = new ArrayList<>();

        userPermissionEntities.forEach(userPermission -> {
            Optional<UserPermissionEntity> existingUserPermission = userPermissionRepository
                    .findByObjectIdentifier(userPermission.getObjectIdentifier());
            if (existingUserPermission.isEmpty()) {
                newUserPermissionEntityList.add(userPermission);
            }
        });

        userPermissionRepository.saveAll(newUserPermissionEntityList);
    }

    private void deleteUserPermissionsNotInList(List<UserPermissionEntity> userPermissionEntities) {
        List<UserPermissionEntity> allCurrentUserPermissionEntities = userPermissionRepository.findAll();

        List<String> inputUserPermissionIdentifiers = userPermissionEntities.stream()
                .map(UserPermissionEntity::getObjectIdentifier)
                .toList();

        List<UserPermissionEntity> userPermissionsToDeleteEntity = allCurrentUserPermissionEntities.stream()
                .filter(userPermission -> !inputUserPermissionIdentifiers.contains(userPermission.getObjectIdentifier()))
                .toList();

        userPermissionRepository.deleteAll(userPermissionsToDeleteEntity);

        if (!userPermissionsToDeleteEntity.isEmpty()) {
            log.info("Deleted {} user permissions", userPermissionsToDeleteEntity.size());
        }
    }

}
