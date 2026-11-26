package no.novari.flyt.authorization.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.authorization.user.model.UserPageResponse
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$INTERNAL_API/authorization/users")
@Tag(name = "Users", description = "Administration of Flyt user permissions.")
@PreAuthorize("hasRole('ADMIN')")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    @Operation(summary = "List users")
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort: String,
    ): UserPageResponse {
        val users = userService.getAll(PageRequest.of(page, size, Sort.by(sort)))

        return UserPageResponse(
            content = users.content,
            totalPages = users.totalPages,
        )
    }

    @PostMapping("actions/userPermissionBatchPut")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create or update user permissions in a batch")
    fun postUserPermissionBatchPutAction(
        @RequestBody users: List<User>,
    ) {
        userService.putAll(users)
        userService.publishUsers()
    }
}
