package no.novari.flyt.authorization.user;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPublishingComponent {

    private final UserService userService;

    @Scheduled(
            initialDelayString = "${novari.flyt.authorization.access-control.sync-schedule.initial-delay}",
            fixedDelayString = "${novari.flyt.authorization.access-control.sync-schedule.fixed-delay}"
    )
    public void publishUsers() {
        userService.publishUsers();
    }

}
