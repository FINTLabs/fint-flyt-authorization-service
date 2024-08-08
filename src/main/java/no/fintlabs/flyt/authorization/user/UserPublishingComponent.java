package no.fintlabs.flyt.authorization.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "fint.flyt.authorization.access-control.enabled", havingValue = "true")
@RequiredArgsConstructor
public class UserPublishingComponent {

    private final UserService userService;

    @Scheduled(
            initialDelayString = "${fint.flyt.authorization.access-control.sync-schedule.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.authorization.access-control.sync-schedule.fixed-delay-ms}"
    )
    public void publishUsers() {
        userService.publishUsers();
    }

}
