package no.fintlabs.flyt.authorization.user.controller.utils;

import no.fintlabs.flyt.authorization.user.azure.models.UserDisplayText;
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

    public UserDisplayText getUserDisplayTextFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        return UserDisplayText
                .builder()
                .objectIdentifier(UUID.fromString(tokenAttributes.get("objectidentifier").toString()))
                .name(tokenAttributes.get("").toString()) // TODO eivindmorch 28/06/2024 :
                .email(tokenAttributes.get("").toString()) // TODO eivindmorch 28/06/2024 :
                .build();
    }

    public Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }
}
