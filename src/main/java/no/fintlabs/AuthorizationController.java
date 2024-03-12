package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.models.AuthorizedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization")
@Slf4j
public class AuthorizationController {

    public AuthorizationController() {
    }

    @GetMapping("check-authorized")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("user")
    public Mono<ResponseEntity<AuthorizedUser>> checkUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    if (authentication != null && authentication.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                        return Mono.just(ResponseEntity.ok(AuthorizedUser.builder().admin(true).build()));
                    } else {
                        return Mono.just(ResponseEntity.ok(AuthorizedUser.builder().admin(false).build()));
                    }
                });
    }

}
