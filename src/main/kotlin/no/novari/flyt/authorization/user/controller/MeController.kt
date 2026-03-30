package no.novari.flyt.authorization.user.controller

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
import java.util.UUID

@RestController
@RequestMapping("$INTERNAL_API/authorization/me")
class MeController(
    private val tokenParsingUtils: TokenParsingUtils,
    private val userService: UserService,
    private val sourceApplications: List<SourceApplication>,
) {
    @GetMapping("is-authorized")
    fun checkAuthorization(authentication: Authentication?): String {
        val jwtAuthToken = requireJwtAuthenticationToken(authentication)
        if (getUserFromUserAuthorizationComponent(jwtAuthToken) == null &&
            tokenParsingUtils.hasPermittedRole(jwtAuthToken)
        ) {
            createUserFromToken(authentication)
        }

        return "User authorized"
    }

    @GetMapping("restricted-page-authorization")
    fun getRestrictedPageAuthorization(authentication: Authentication?): RestrictedPageAuthorization {
        return RestrictedPageAuthorization(
            userPermissionPage = tokenParsingUtils.isAdmin(authentication),
        )
    }

    @GetMapping
    fun get(authentication: Authentication?): User {
        val jwtAuthToken = requireJwtAuthenticationToken(authentication)

        return getUserFromUserAuthorizationComponent(jwtAuthToken) ?: createUserFromToken(authentication)
    }

    private fun getUserFromUserAuthorizationComponent(token: JwtAuthenticationToken): User? {
        return userService.find(UUID.fromString(tokenParsingUtils.getObjectIdentifierFromToken(token)))
    }

    private fun createUserFromToken(authentication: Authentication?): User {
        val jwtAuthToken = requireJwtAuthenticationToken(authentication)
        val user = tokenParsingUtils.getUserFromToken(jwtAuthToken)
        val newUser =
            if (tokenParsingUtils.isAdmin(authentication)) {
                user.copy(sourceApplicationIds = allSourceApplicationIds())
            } else {
                user
            }

        userService.save(newUser)
        return newUser
    }

    private fun allSourceApplicationIds(): List<Long> {
        return sourceApplications.map(SourceApplication::id).sorted()
    }

    private fun requireJwtAuthenticationToken(authentication: Authentication?): JwtAuthenticationToken {
        return authentication as? JwtAuthenticationToken
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required")
    }
}
