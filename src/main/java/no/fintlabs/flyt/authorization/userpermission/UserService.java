package no.fintlabs.flyt.authorization.userpermission;

import no.fintlabs.flyt.azure.repositories.UserDisplayTextCacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserPermissionService userPermissionService;
    private final UserDisplayTextCacheRepository userDisplayTextCacheRepository;

    public UserService(
            UserPermissionService userPermissionService,
            UserDisplayTextCacheRepository userDisplayTextCacheRepository
    ) {
        this.userPermissionService = userPermissionService;
        this.userDisplayTextCacheRepository = userDisplayTextCacheRepository;
    }

    public Optional<User> getUser(String objectIdentifier) {
        return userPermissionService.get(objectIdentifier)
                .map(this::mapToUserWithDisplayText);
    }

    public List<User> getUsers() {
        return userPermissionService.getAll()
                .stream()
                .map(this::mapToUserWithDisplayText)
                .toList();
    }

    public List<User> putUsers(List<UserPermission> userPermissions) {
        return userPermissionService.putAll(userPermissions)
                .stream()
                .map(this::mapToUserWithDisplayText)
                .toList();
    }

    private User mapToUserWithDisplayText(UserPermission userPermission) {
        User.UserBuilder userBuilder = User
                .builder()
                .objectIdentifier(userPermission.getObjectIdentifier())
                .sourceApplicationIds(userPermission.getSourceApplicationIds());

        userDisplayTextCacheRepository.findByObjectIdentifier(userPermission.getObjectIdentifier())
                .ifPresent(userDisplayText -> userBuilder
                        .email(userDisplayText.getEmail())
                        .name(userDisplayText.getName()));

        return userBuilder.build();
    }
}
