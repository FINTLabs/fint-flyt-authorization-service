package no.fintlabs.flyt.authorization;

import no.fintlabs.flyt.authorization.userpermission.UserPermissionEntity;
import no.fintlabs.flyt.authorization.userpermission.UserPermission;
import no.fintlabs.flyt.azure.models.UserDisplayText;
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

//    public UserPermission buildUserPermissionDto(UserPermissionEntity userPermissionEntity) {
//
//        UserDisplayText userDisplayText = azureUserCacheRepository.findByObjectIdentifier(userPermissionEntity.getObjectIdentifier());
//
//        return UserPermission
//                .builder()
//                .objectIdentifier(userPermissionEntity.getObjectIdentifier())
//                .email(userDisplayText != null ? userDisplayText.getEmail() : null)
//                .name(userDisplayText != null ? userDisplayText.getName() : null)
//                .sourceApplicationIds(userPermissionEntity.getSourceApplicationIds())
//                .build();
//    }
}
