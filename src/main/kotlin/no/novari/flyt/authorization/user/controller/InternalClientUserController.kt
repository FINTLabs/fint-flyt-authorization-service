package no.novari.flyt.authorization.user.controller

import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_CLIENT_API
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("$INTERNAL_CLIENT_API/authorization/brukere")
class InternalClientUserController(
    private val userService: UserService,
) {
    @GetMapping("/{objectIdentifier}")
    fun get(
        @PathVariable objectIdentifier: UUID,
    ): User {
        return userService.find(objectIdentifier)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/actions/lookup")
    fun lookup(
        @RequestBody objectIdentifiers: List<UUID>,
    ): List<User> {
        return userService.findAllByObjectIdentifiers(objectIdentifiers)
    }
}
