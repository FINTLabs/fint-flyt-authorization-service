package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.azure.models.GraphUserInfo;
import no.fintlabs.flyt.authorization.user.azure.services.GraphService;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "fint.flyt.azure-ad-gateway.enable", havingValue = "true")
@Slf4j
public class AzureAdUserAuthorizationComponent {

    private final GraphService graphService;
    private final UserService userService;

    public AzureAdUserAuthorizationComponent(
            GraphService graphService,
            UserService userService
    ) {
        this.graphService = graphService;
        this.userService = userService;
    }

    @Scheduled(
            initialDelayString = "${fint.flyt.azure-ad-gateway.sync-schedule.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.azure-ad-gateway.sync-schedule.fixed-delay-ms}"
    )
    public void syncUsers() {
        if (!graphService.areCredentialsAvailable()) {
            log.warn("Skipping syncUsers because credentials are not available.");
            return;
        }

        log.info("Syncing users");
        updateUsers(graphService.getPermittedUsersInfo());
        log.info("Successfully synced users");
    }

    private void updateUsers(Collection<GraphUserInfo> permittedUsersGraphUserInfo) {
        log.info("Updating users based on {} permitted user info", permittedUsersGraphUserInfo.size());

        List<User> usersToUpdate = permittedUsersGraphUserInfo.stream()
                .map(graphUserInfo -> User
                        .builder()
                        .objectIdentifier(graphUserInfo.getId())
                        .name(graphUserInfo.getDisplayName())
                        .email(graphUserInfo.getMail())
                        .build()
                ).toList();

        userService.updateUsers(usersToUpdate);
        log.info("Successfully updated users");
    }

    public Optional<User> getUser(UUID objectIdentifier) {
        return userService.find(objectIdentifier);
    }

    public Page<User> getUsers(Pageable pageable) {
        return userService.getAll(pageable);
    }

    public void batchPutUserPermissions(List<User> users) {
        userService.putAll(users);
    }

}
