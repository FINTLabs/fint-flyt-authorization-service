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

    public void saveUserPermissions(List<UserPermission> userPermissions) {

        nullifyUserPermissionsNotInList(userPermissions);

        List<UserPermission> newUserPermissionList = new ArrayList<>();

        userPermissions.forEach(userPermission -> {
            Optional<UserPermission> existingPermission = userPermissionRepository
                    .findByObjectIdentifier(userPermission.getObjectIdentifier());
            if (existingPermission.isEmpty()) {
                newUserPermissionList.add(userPermission);
            }
        });

        userPermissionRepository.saveAll(newUserPermissionList);
    }

    private void nullifyUserPermissionsNotInList(List<UserPermission> userPermissions) {
        List<UserPermission> allCurrentUserPermissions = userPermissionRepository.findAll();

        List<String> inputUserPermissionIdentifiers = userPermissions.stream()
                .map(UserPermission::getObjectIdentifier)
                .toList();

        List<UserPermission> userPermissionsToNullify = allCurrentUserPermissions.stream()
                .filter(userPermission -> !inputUserPermissionIdentifiers.contains(userPermission.getObjectIdentifier()))
                .toList();

        userPermissionsToNullify.forEach(userPermission -> userPermission.setSourceApplicationIds(null));

        userPermissionRepository.saveAll(userPermissionsToNullify);
    }

}
