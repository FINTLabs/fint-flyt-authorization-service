package no.novari.flyt.authorization.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

/**
 * `@PreAuthorize` on the controllers. fint-flyt-web-resource-server gives a DEVELOPER also `ROLE_ADMIN`, so
 * `hasRole('ADMIN')` lets both through. flyt-security-starter turns this on itself.
 */
@Configuration
@EnableMethodSecurity
class MethodSecurityConfiguration
