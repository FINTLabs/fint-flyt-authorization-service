package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;

    public UserPermissionService(UserPermissionRepository userPermissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
    }

    public void refreshUserPermissions(List<UserPermission> userPermissions) {

        deleteUserPermissionsNotInList(userPermissions);

        List<UserPermission> newUserPermissionList = new ArrayList<>();

        userPermissions.forEach(userPermission -> {
            Optional<UserPermission> existingUserPermission = userPermissionRepository
                    .findByObjectIdentifier(userPermission.getObjectIdentifier());
            if (existingUserPermission.isEmpty()) {
                newUserPermissionList.add(userPermission);
            }
        });

        userPermissionRepository.saveAll(newUserPermissionList);
    }

    private void deleteUserPermissionsNotInList(List<UserPermission> userPermissions) {
        List<UserPermission> allCurrentUserPermissions = userPermissionRepository.findAll();

        List<String> inputUserPermissionIdentifiers = userPermissions.stream()
                .map(UserPermission::getObjectIdentifier)
                .toList();

        List<UserPermission> userPermissionsToDelete = allCurrentUserPermissions.stream()
                .filter(userPermission -> !inputUserPermissionIdentifiers.contains(userPermission.getObjectIdentifier()))
                .toList();

        userPermissionRepository.deleteAll(userPermissionsToDelete);

        if (!userPermissionsToDelete.isEmpty()) {
            log.info("Deleted {} user permissions", userPermissionsToDelete.size());
        }
    }

}
