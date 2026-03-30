package no.novari.flyt.authorization.user.controller

import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.controller.utils.TokenParsingUtils
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$INTERNAL_API/authorization/users")
class UserController(
    private val tokenParsingUtils: TokenParsingUtils,
    private val userService: UserService,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort: String,
    ): Page<User> {
        requireAdmin(authentication)

        return userService.getAll(PageRequest.of(page, size, Sort.by(sort)))
    }

    @PostMapping("actions/userPermissionBatchPut")
    @ResponseStatus(HttpStatus.OK)
    fun postUserPermissionBatchPutAction(
        @AuthenticationPrincipal authentication: Authentication,
        @RequestBody users: List<User>,
    ) {
        requireAdmin(authentication)
        userService.putAll(users)
        userService.publishUsers()
    }

    private fun requireAdmin(authentication: Authentication) {
        if (!tokenParsingUtils.isAdmin(authentication)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
    }
}
