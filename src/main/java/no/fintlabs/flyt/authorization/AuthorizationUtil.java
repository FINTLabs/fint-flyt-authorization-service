package no.fintlabs.flyt.authorization;

import no.fintlabs.flyt.authorization.user.UserPermission;
import no.fintlabs.flyt.authorization.user.UserPermissionDto;
import no.fintlabs.flyt.azure.models.AzureUserCache;
import no.fintlabs.flyt.azure.repositories.AzureUserCacheRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthorizationUtil {

    private final AzureUserCacheRepository azureUserCacheRepository;

    public AuthorizationUtil(AzureUserCacheRepository azureUserCacheRepository) {
        this.azureUserCacheRepository = azureUserCacheRepository;
    }

    public String getObjectIdentifierFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getTokenAttributes().get("objectidentifier").toString();
    }

    public Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }

    public UserPermissionDto buildUserPermissionDto(UserPermission userPermission) {

        AzureUserCache azureUserCache = azureUserCacheRepository.findByObjectIdentifier(userPermission.getObjectIdentifier());

        return UserPermissionDto
                .builder()
                .objectIdentifier(userPermission.getObjectIdentifier())
                .email(azureUserCache != null ? azureUserCache.getEmail() : null)
                .name(azureUserCache != null ? azureUserCache.getName() : null)
                .sourceApplicationIds(userPermission.getSourceApplicationIds())
                .build();
    }
}
