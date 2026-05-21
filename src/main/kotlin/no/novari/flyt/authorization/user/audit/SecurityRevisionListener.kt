package no.novari.flyt.authorization.user.audit

import org.hibernate.envers.RevisionListener
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SecurityRevisionListener : RevisionListener {
    override fun newRevision(revisionEntity: Any) {
        val revisionInfo = revisionEntity as RevisionInfo
        revisionInfo.actor = currentActor()
    }

    private fun currentActor(): String {
        val authentication = SecurityContextHolder.getContext().authentication ?: return RevisionInfo.SYSTEM_ACTOR

        val jwtAuthenticationToken = authentication as? JwtAuthenticationToken

        return jwtAuthenticationToken
            ?.tokenAttributes
            ?.get("email")
            ?.toString()
            ?.takeIf(String::isNotBlank)
            ?: jwtAuthenticationToken
                ?.tokenAttributes
                ?.get(
                    "preferred_username",
                )?.toString()
                ?.takeIf(String::isNotBlank)
            ?: jwtAuthenticationToken
                ?.tokenAttributes
                ?.get("oid")
                ?.toString()
                ?.takeIf(String::isNotBlank)
            ?: authentication.name.takeIf(String::isNotBlank)
            ?: RevisionInfo.SYSTEM_ACTOR
    }
}
