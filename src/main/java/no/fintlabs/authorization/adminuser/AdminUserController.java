package no.fintlabs.authorization.adminuser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/adminuser")
@Slf4j
public class AdminUserController {

    public AdminUserController() {
    }

    @GetMapping("check-authorized")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("user")
    public Mono<ResponseEntity<AdminUser>> checkAdminUser(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono
                .map(authentication -> {
                    if (authentication != null && authentication.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                        return ResponseEntity.ok(AdminUser.builder().admin(true).build());
                    } else {
                        return ResponseEntity.ok(AdminUser.builder().admin(false).build());
                    }
                });
    }

//
//
//    // todo move  to AdminUserController
//    @PostMapping("sourceapplication")
//    public Mono<ResponseEntity<UserPermission>> setSourceApplications(
//            @RequestBody UserPermissionDto userPermissionDto,
//            @AuthenticationPrincipal Mono<Authentication> authenticationMono
//    ) {
//        authenticationMono
//                .map(this::getSub)
//                        .
//
//
//
//                userPermissionRepository.save(UserPermission.builder().build())
//
//        return Mono.just(ResponseEntity.ok(
//                UserPermission
//                        .builder()
//                        .sub(getSub(authenticationMono))
//                        .sourceApplicationIds(userPermissionDto.getSourceApplicationIds())
//                        .build())
//        );
//
//
//        UserPermission userPermission = UserPermission
//                .builder()
//                .sub(getSub(authenticationMono))
//                .sourceApplicationIds(userPermissionDto.getSourceApplicationIds())
//                .build();
//
//        return (ResponseEntity.ok(userPermission));
//    }
//
//    public String getSub(Authentication authentication) {
//        if (authentication == null) {
//            throw new IllegalStateException("Authentication cannot be null");
//        }
//
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            throw  new IllegalStateException("Principal is not of type JWT");
//        }
//
//        return getSubFromToken((JwtAuthenticationToken) authentication);
//    }
//
//    public String getSubFromToken(JwtAuthenticationToken jwtAuthenticationToken) {
//        return jwtAuthenticationToken.getName();
//    }

}
