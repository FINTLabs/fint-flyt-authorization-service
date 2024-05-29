package no.fintlabs.authorization.adminuser;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.authorization.user.UserPermission;
import no.fintlabs.authorization.user.UserPermissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/adminuser")
@Slf4j
public class AdminUserController {

    private final UserPermissionRepository userPermissionRepository;

    public AdminUserController(
            UserPermissionRepository userPermissionRepository
    ) {
        this.userPermissionRepository = userPermissionRepository;
    }


    @GetMapping("check-is-admin")
    public Mono<ResponseEntity<AdminUser>> checkAdminUser(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return isAdmin(authenticationMono)
                .map(isAdmin -> ResponseEntity.ok(AdminUser.builder().admin(isAdmin).build()));
    }

    @GetMapping("userpermissions")
    public Mono<ResponseEntity<List<UserPermission>>> getUserPermissions(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Mono.fromCallable(userPermissionRepository::findAll)
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(userPermissions -> ResponseEntity.ok().body(userPermissions));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }

    private Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }

}
