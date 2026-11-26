package no.novari.flyt.authorization.user.controller.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.authorization.user.AccessControlProperties
import no.novari.flyt.authorization.user.model.User
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenParsingUtils(
    private val accessControlProperties: AccessControlProperties,
    private val roleHierarchy: RoleHierarchy,
) {
    private val log = KotlinLogging.logger {}

    fun getObjectIdentifierFromToken(jwtAuthenticationToken: JwtAuthenticationToken): String {
        return requiredTokenAttribute(jwtAuthenticationToken, OBJECT_IDENTIFIER)
    }

    fun getUserFromToken(jwtAuthenticationToken: JwtAuthenticationToken): User {
        return User(
            objectIdentifier =
                UUID.fromString(
                    requiredTokenAttribute(jwtAuthenticationToken, OBJECT_IDENTIFIER),
                ),
            name = jwtAuthenticationToken.tokenAttributes["displayname"]?.toString().orEmpty(),
            email = jwtAuthenticationToken.tokenAttributes["email"]?.toString(),
        )
    }

    fun getRolesFromToken(jwtAuthenticationToken: JwtAuthenticationToken): List<String> {
        return (jwtAuthenticationToken.tokenAttributes[ROLES] as? List<*>)
            ?.mapNotNull { role -> role?.toString() }
            .orEmpty()
    }

    fun hasPermittedRole(jwtAuthenticationToken: JwtAuthenticationToken): Boolean {
        val roles = getRolesFromToken(jwtAuthenticationToken)
        if (roles.isEmpty()) {
            log.atWarn { message = "Roles are null or empty in token" }
            return false
        }

        val permittedRoles = accessControlProperties.permittedAppRoles.values
        if (permittedRoles.isEmpty()) {
            log.atWarn { message = "Permitted app roles are not configured or empty" }
            return false
        }

        return roles.any(permittedRoles::contains)
    }

    /** Through the role hierarchy of flyt-security-starter, so a DEVELOPER is also an admin. */
    fun isAdmin(authentication: Authentication?): Boolean {
        val authorities = authentication?.authorities ?: return false
        return roleHierarchy.getReachableGrantedAuthorities(authorities).any { it.authority == "ROLE_ADMIN" }
    }

    private fun requiredTokenAttribute(
        jwtAuthenticationToken: JwtAuthenticationToken,
        claim: String,
    ): String {
        return jwtAuthenticationToken.tokenAttributes[claim]?.toString()
            ?: throw IllegalArgumentException("Missing token claim: $claim")
    }

    private companion object {
        const val OBJECT_IDENTIFIER = "objectidentifier"
        const val ROLES = "roles"
    }
}
