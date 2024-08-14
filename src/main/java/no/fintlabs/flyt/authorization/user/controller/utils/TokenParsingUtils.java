package no.fintlabs.flyt.authorization.user.controller.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.AccessControlProperties;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenParsingUtils {

    private final AccessControlProperties accessControlProperties;

    public String getObjectIdentifierFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getTokenAttributes().get("objectidentifier").toString();
    }

    public User getUserFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        return User
                .builder()
                .objectIdentifier(UUID.fromString(tokenAttributes.get("objectidentifier").toString()))
                .name(tokenAttributes.getOrDefault("displayname", "").toString())
                .email(tokenAttributes.get("email").toString())
                .build();
    }

    public List<String> getRolesFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return ((List<?>) jwtAuthenticationToken.getTokenAttributes().get("roles"))
                .stream()
                .map(Object::toString)
                .toList();
    }

    public boolean hasPermittedRole(JwtAuthenticationToken jwtAuthenticationToken) {
        List<String> roles = getRolesFromToken(jwtAuthenticationToken);
        if (roles == null || roles.isEmpty()) {
            log.warn("Roles are null or empty in token");
            return false;
        }
        Map<String, String> permittedRolesMap = accessControlProperties.getPermittedAppRoles();
        if (permittedRolesMap == null || permittedRolesMap.isEmpty()) {
            log.warn("Permitted app roles are not configured or empty");
            return false;
        }
        List<String> permittedRoles = permittedRolesMap.values().stream().toList();
        return roles.stream().anyMatch(permittedRoles::contains);
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}
