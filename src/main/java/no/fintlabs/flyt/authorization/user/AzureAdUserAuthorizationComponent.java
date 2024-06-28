package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.authorization.user.azure.models.GraphUserInfo;
import no.fintlabs.flyt.authorization.user.azure.models.UserDisplayText;
import no.fintlabs.flyt.authorization.user.azure.services.GraphService;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.model.UserPermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Component
@ConditionalOnProperty(value = "fint.flyt.azure-ad-gateway.enable", havingValue = "true")
@Slf4j
public class AzureAdUserAuthorizationComponent {

    private final GraphService graphService;

    private final UserPermissionService userPermissionService;

    private final FintCache<UUID, UserDisplayText> userDisplayTextCache;

    public AzureAdUserAuthorizationComponent(
            GraphService graphService, UserPermissionService userPermissionService,
            FintCache<UUID, UserDisplayText> userDisplayTextCache
    ) {
        this.graphService = graphService;
        this.userPermissionService = userPermissionService;
        this.userDisplayTextCache = userDisplayTextCache;
    }

    @Scheduled(
            initialDelayString = "${fint.flyt.azure-ad-gateway.sync-schedule.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.azure-ad-gateway.sync-schedule.fixed-delay-ms}"
    )
    public void syncUsers() {
        log.info("Syncing users");
        updateUsers(graphService.getPermittedUsersInfo());
        log.info("Successfully synced users");
    }

    private void updateUsers(Collection<GraphUserInfo> permittedUsersGraphUserInfo) {
        log.info("Updating users based on {} permitted user info", permittedUsersGraphUserInfo.size());

        Set<UUID> objectIdentifiers = permittedUsersGraphUserInfo.stream()
                .map(GraphUserInfo::getId)
                .collect(Collectors.toSet());

        userPermissionService.updateUserPermissions(objectIdentifiers);

        Set<UUID> objectIdentifiersAlreadyCachedButNotInNewUserInfo = userDisplayTextCache.getAll()
                .stream()
                .map(UserDisplayText::getObjectIdentifier)
                .filter(objectIdentifier -> !objectIdentifiers.contains(objectIdentifier))
                .collect(Collectors.toSet());

        log.info("Removing {} user display text with objectIdentifiers: {}",
                objectIdentifiersAlreadyCachedButNotInNewUserInfo.size(),
                objectIdentifiersAlreadyCachedButNotInNewUserInfo
        );
        userDisplayTextCache.remove(objectIdentifiersAlreadyCachedButNotInNewUserInfo);
        log.info("Putting {} user display text", permittedUsersGraphUserInfo);
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

    public Page<User> getUsers(Pageable pageable) {
        return userPermissionService.getAll(pageable)
                .map(this::mapToUserWithDisplayText);
    }

    public List<User> batchPutUserPermissions(List<UserPermission> userPermissions) {
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
