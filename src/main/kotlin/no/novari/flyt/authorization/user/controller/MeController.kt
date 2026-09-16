package no.novari.flyt.authorization.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import no.novari.flyt.authorization.user.UserService
import no.novari.flyt.authorization.user.controller.utils.TokenParsingUtils
import no.novari.flyt.authorization.user.model.RestrictedPageAuthorization
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$INTERNAL_API/authorization/me")
@Tag(name = "Current user", description = "Authorization and permissions for the authenticated user.")
class MeController(
    private val tokenParsingUtils: TokenParsingUtils,
    private val userService: UserService,
    private val sourceApplications: List<SourceApplication>,
) {
    @GetMapping("is-authorized")
    @Operation(summary = "Verify that the current user is authorized")
    fun checkAuthorization(authentication: Authentication?): String {
        val jwtAuthToken = requireJwtAuthenticationToken(authentication)
        if (tokenParsingUtils.hasPermittedRole(jwtAuthToken)) {
            userService.findOrCreate(buildUserFromToken(jwtAuthToken, authentication))
        }

        return "User authorized"
    }

    @GetMapping("restricted-page-authorization")
    @Operation(summary = "Get restricted-page authorization for the current user")
    fun getRestrictedPageAuthorization(authentication: Authentication?): RestrictedPageAuthorization {
        return RestrictedPageAuthorization(
            userPermissionPage = tokenParsingUtils.isAdmin(authentication),
        )
    }

    @GetMapping
    @Operation(summary = "Get or provision the current user")
    fun get(authentication: Authentication?): User {
        val jwtAuthToken = requireJwtAuthenticationToken(authentication)
        return userService.findOrCreate(buildUserFromToken(jwtAuthToken, authentication))
    }

    private fun buildUserFromToken(
        jwtAuthToken: JwtAuthenticationToken,
        authentication: Authentication?,
    ): User {
        val user = tokenParsingUtils.getUserFromToken(jwtAuthToken)
        return if (tokenParsingUtils.isAdmin(authentication)) {
            user.copy(sourceApplicationIds = allSourceApplicationIds())
        } else {
            user
        }
    }

    private fun allSourceApplicationIds(): List<Long> {
        return sourceApplications.map(SourceApplication::id).sorted()
    }

    private fun requireJwtAuthenticationToken(authentication: Authentication?): JwtAuthenticationToken {
        return authentication as? JwtAuthenticationToken
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required")
    }
}
