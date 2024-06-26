package no.fintlabs.authorization;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationUtil {
    public String getObjectIdentifierFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getTokenAttributes().get("objectidentifier").toString();
    }
}
