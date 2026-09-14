package no.novari.flyt.authorization.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.controller.model.SourceApplicationAuthorizationRequest
import no.novari.flyt.authorization.user.controller.model.SourceApplicationAuthorizationResponse
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
@RequestMapping("$INTERNAL_CLIENT_API/authorization/users")
@Tag(name = "Client users", description = "User authorization for trusted service clients.")
class InternalClientUserController(
    private val userService: UserService,
) {
    @GetMapping("/{objectIdentifier}")
    @Operation(summary = "Get a user by object identifier")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User found"),
            ApiResponse(responseCode = "404", description = "User not found"),
        ],
    )
    fun get(
        @Parameter(description = "The user's object identifier")
        @PathVariable objectIdentifier: UUID,
    ): User {
        return userService.find(objectIdentifier)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/actions/lookup")
    @Operation(summary = "Look up users by object identifiers")
    fun lookup(
        @RequestBody objectIdentifiers: List<UUID>,
    ): List<User> {
        return userService.findAllByObjectIdentifiers(objectIdentifiers)
    }

    @PostMapping("/actions/authorize-source-applications")
    @Operation(summary = "Get the source applications authorized for a user")
    fun authorizeSourceApplications(
        @RequestBody request: SourceApplicationAuthorizationRequest,
    ): SourceApplicationAuthorizationResponse {
        if (request.sourceApplicationIds.isEmpty()) {
            return SourceApplicationAuthorizationResponse(emptySet())
        }

        val authorizedSourceApplicationIds =
            userService.findAuthorizedSourceApplicationIds(
                request.objectIdentifier,
                request.sourceApplicationIds,
            )

        return SourceApplicationAuthorizationResponse(
            authorizedSourceApplicationIds,
        )
    }
}
