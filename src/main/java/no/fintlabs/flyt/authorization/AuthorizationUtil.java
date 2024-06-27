package no.fintlabs.flyt.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthorizationUtil {
    public String getObjectIdentifierFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getTokenAttributes().get("objectidentifier").toString();
    }

    public Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }
}
