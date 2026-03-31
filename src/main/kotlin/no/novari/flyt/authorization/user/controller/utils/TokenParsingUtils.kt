package no.novari.flyt.authorization.user.controller.utils

import no.novari.flyt.authorization.user.AccessControlProperties
import no.novari.flyt.authorization.user.model.User
import no.novari.flyt.webresourceserver.security.user.UserClaim
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenParsingUtils(
    private val accessControlProperties: AccessControlProperties,
) {
    fun getObjectIdentifierFromToken(jwtAuthenticationToken: JwtAuthenticationToken): String {
        return requiredTokenAttribute(jwtAuthenticationToken, UserClaim.OBJECT_IDENTIFIER)
    }

    fun getUserFromToken(jwtAuthenticationToken: JwtAuthenticationToken): User {
        return User(
            objectIdentifier =
                UUID.fromString(
                    requiredTokenAttribute(jwtAuthenticationToken, UserClaim.OBJECT_IDENTIFIER),
                ),
            name = jwtAuthenticationToken.tokenAttributes["displayname"]?.toString().orEmpty(),
            email = jwtAuthenticationToken.tokenAttributes["email"]?.toString(),
        )
    }

    fun getRolesFromToken(jwtAuthenticationToken: JwtAuthenticationToken): List<String> {
        return (jwtAuthenticationToken.tokenAttributes[UserClaim.ROLES.tokenClaimName] as? List<*>)
            ?.mapNotNull { role -> role?.toString() }
            .orEmpty()
    }

    fun hasPermittedRole(jwtAuthenticationToken: JwtAuthenticationToken): Boolean {
        val roles = getRolesFromToken(jwtAuthenticationToken)
        if (roles.isEmpty()) {
            logger.warn("Roles are null or empty in token")
            return false
        }

        val permittedRoles = accessControlProperties.permittedAppRoles.values
        if (permittedRoles.isEmpty()) {
            logger.warn("Permitted app roles are not configured or empty")
            return false
        }

        return roles.any(permittedRoles::contains)
    }

    fun isAdmin(authentication: Authentication?): Boolean {
        return authentication?.authorities?.any { grantedAuthority ->
            grantedAuthority.authority == "ROLE_ADMIN"
        } == true
    }

    private fun requiredTokenAttribute(
        jwtAuthenticationToken: JwtAuthenticationToken,
        claim: UserClaim,
    ): String {
        return jwtAuthenticationToken.tokenAttributes[claim.tokenClaimName]?.toString()
            ?: throw IllegalArgumentException("Missing token claim: ${claim.tokenClaimName}")
    }

    private companion object {
        val logger = LoggerFactory.getLogger(TokenParsingUtils::class.java)
    }
}
