package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "fint.flyt.authorization.access-control.enabled", havingValue = "true")
@Slf4j
public class UserPublishingComponent {

    private final UserService userService;

    public UserPublishingComponent(
            UserService userService
    ) {
        this.userService = userService;
    }

    @Scheduled(
            initialDelayString = "${fint.flyt.authorization.access-control.sync-schedule.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.authorization.access-control.sync-schedule.fixed-delay-ms}"
    )
    public void publishUsers() {
        log.info("Publishing users");
        userService.publishUsers();
        log.info("Successfully published users");
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
