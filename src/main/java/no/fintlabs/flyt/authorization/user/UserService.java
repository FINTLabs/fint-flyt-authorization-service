package no.fintlabs.flyt.authorization.user;

import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.permission.UserPermissionService;
import no.fintlabs.flyt.authorization.user.permission.model.UserPermission;
import no.fintlabs.flyt.azure.models.GraphUserInfo;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import no.fintlabs.flyt.azure.services.GraphService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class UserService {

    private final GraphService graphService;

    private final UserPermissionService userPermissionService;

    private final FintCache<UUID, UserDisplayText> userDisplayTextCache;

    public UserService(
            UserPermissionService userPermissionService,
            FintCache<UUID, UserDisplayText> userDisplayTextCache,
            GraphService graphService) {
        this.userPermissionService = userPermissionService;
        this.userDisplayTextCache = userDisplayTextCache;
        this.graphService = graphService;
    }

    // TODO eivindmorch 27/06/2024 : Schedule this
    public void syncUsers() {
        updateUsers(graphService.getPermittedUsersInfo());
    }

    private void updateUsers(Collection<GraphUserInfo> permittedUsersGraphUserInfo) {
        Set<UUID> objectIdentifiers = permittedUsersGraphUserInfo.stream()
                .map(GraphUserInfo::getId)
                .collect(Collectors.toSet());

        userPermissionService.updateUserPermissions(objectIdentifiers);

        Set<UUID> objectIdentifiersAlreadyCachedButNotInNewUserInfo = userDisplayTextCache.getAll()
                .stream()
                .map(UserDisplayText::getObjectIdentifier)
                .filter(objectIdentifier -> !objectIdentifiers.contains(objectIdentifier))
                .collect(Collectors.toSet());

        userDisplayTextCache.remove(objectIdentifiersAlreadyCachedButNotInNewUserInfo);
        userDisplayTextCache.put(permittedUsersGraphUserInfo
                .stream()
                .collect(toMap(
                        GraphUserInfo::getId,
                        graphUserInfo -> UserDisplayText
                                .builder()
                                .objectIdentifier(graphUserInfo.getId())
                                .name(graphUserInfo.getDisplayName())
                                .email(graphUserInfo.getMail())
                                .build()
                ))
        );

    }

    public Optional<User> getUser(UUID objectIdentifier) {
        return userPermissionService.find(objectIdentifier)
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

        userDisplayTextCache.getOptional(userPermission.getObjectIdentifier())
                .ifPresent(userDisplayText -> userBuilder
                        .email(userDisplayText.getEmail())
                        .name(userDisplayText.getName()));

        return userBuilder.build();
    }

}
