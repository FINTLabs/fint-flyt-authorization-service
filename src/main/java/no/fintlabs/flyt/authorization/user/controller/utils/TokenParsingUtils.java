package no.fintlabs.flyt.authorization.user.controller.utils;

import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class TokenParsingUtils {
    public String getObjectIdentifierFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getTokenAttributes().get("objectidentifier").toString();
    }

    public User getUserFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        return User
                .builder()
                .objectIdentifier(UUID.fromString(tokenAttributes.get("objectidentifier").toString()))
                .name(tokenAttributes.get("displayname").toString())
                .email(tokenAttributes.get("email").toString())
                .build();
    }

    public Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }
}
