package no.fintlabs.flyt.authorization.user;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.AuthorizationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@Slf4j
@RequestMapping(INTERNAL_API + "/authorization/user")
@RestController
public class UserPermissionController {

    private final UserPermissionRepository userPermissionRepository;
    private final AuthorizationUtil authorizationUtil;

    public UserPermissionController(
            UserPermissionRepository userPermissionRepository,
            AuthorizationUtil authorizationUtil
    ) {
        this.userPermissionRepository = userPermissionRepository;
        this.authorizationUtil = authorizationUtil;
    }

    @GetMapping("check-authorized")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("permission")
    public Mono<ResponseEntity<UserPermissionDto>> getSourceApplications(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono
                .map(authentication -> authorizationUtil
                        .getObjectIdentifierFromToken((JwtAuthenticationToken) authentication))
                .publishOn(Schedulers.boundedElastic())
                .map(userPermissionRepository::findByObjectIdentifier)
                .map(optionalUserPermission -> optionalUserPermission.map(userPermission -> ResponseEntity.ok(
                                UserPermissionDto
                                        .builder()
                                        .objectIdentifier(userPermission.getObjectIdentifier())
                                        .sourceApplicationIds(userPermission.getSourceApplicationIds())
                                        .build()
                        )).orElseGet(() -> ResponseEntity.notFound().build())
                );
    }

}
