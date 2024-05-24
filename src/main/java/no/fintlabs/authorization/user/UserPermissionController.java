package no.fintlabs.authorization.user;

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

@RequestMapping(INTERNAL_API + "/authorization/user")
@RestController
public class UserPermissionController {

    private UserPermissionRepository userPermissionRepository;

    @GetMapping("sourceapplication")
    public Mono<ResponseEntity<UserPermission>> getSourceApplications(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {

        return authenticationMono
                .map(this::getSub)
                .publishOn(Schedulers.boundedElastic())
                .map(sub -> userPermissionRepository.findBySub(sub))
                .map(optionalUserPermission -> optionalUserPermission
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build())
                );
    }

    public String getSub(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication cannot be null");
        }

        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new IllegalStateException("Principal is not of type JWT");
        }

        return getSubFromToken((JwtAuthenticationToken) authentication);
    }

    public String getSubFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getName();
    }

}
