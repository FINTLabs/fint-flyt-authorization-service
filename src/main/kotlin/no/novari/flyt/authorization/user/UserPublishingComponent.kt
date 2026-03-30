package no.novari.flyt.authorization.user

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserPublishingComponent(
    private val userService: UserService,
) {
    @Scheduled(
        initialDelayString = "\${novari.flyt.authorization.access-control.sync-schedule.initial-delay}",
        fixedDelayString = "\${novari.flyt.authorization.access-control.sync-schedule.fixed-delay}",
    )
    fun publishUsers() {
        userService.publishUsers()
    }
}
