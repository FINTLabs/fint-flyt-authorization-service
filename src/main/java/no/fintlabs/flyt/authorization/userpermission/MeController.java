package no.fintlabs.flyt.authorization.userpermission;

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

@RequestMapping(INTERNAL_API + "/authorization/me")
@RestController
public class MeController {

    private final AuthorizationUtil authorizationUtil;
    private final UserService userService;

    public MeController(
            AuthorizationUtil authorizationUtil,
            UserService userService
    ) {
        this.authorizationUtil = authorizationUtil;
        this.userService = userService;
    }

    @GetMapping("isAuthorized")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("isAdmin")
    public Mono<ResponseEntity<RestrictedPageAccess>> checkAdminUser(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authorizationUtil.isAdmin(authenticationMono)
                .map(isAdmin -> ResponseEntity.ok(RestrictedPageAccess.builder().userPermission(isAdmin).build()));
    }

    @GetMapping
    public Mono<ResponseEntity<User>> get(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono
                .map(authentication -> authorizationUtil
                        .getObjectIdentifierFromToken((JwtAuthenticationToken) authentication))
                .publishOn(Schedulers.boundedElastic())
                .map(userService::getUser)
                .map(optionalUserPermission -> optionalUserPermission.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build())
                );
    }

}
