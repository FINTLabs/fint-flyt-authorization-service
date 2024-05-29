package no.fintlabs.authorization.adminuser;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.authorization.user.UserPermission;
import no.fintlabs.authorization.user.UserPermissionDto;
import no.fintlabs.authorization.user.UserPermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/adminuser")
@Slf4j
public class AdminUserController {

    private final UserPermissionRepository userPermissionRepository;

    public AdminUserController(
            UserPermissionRepository userPermissionRepository) {
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

//    @PostMapping("userpermissions")
//    public Mono<ResponseEntity<List<UserPermissionDto>>> setUserPermission(
//            @RequestBody List<UserPermissionDto> userPermissionDtos,
//            @AuthenticationPrincipal Mono<Authentication> authenticationMono
//    ) {
//        return isAdmin(authenticationMono)
//                .flatMap(isAdmin -> {
//                    if (isAdmin) {
//                        return Flux.fromIterable(userPermissionDtos)
//                                .flatMap(userPermissionDto -> {
//                                    Optional<UserPermission> userPermissionOptional = userPermissionRepository
//                                            .findByObjectIdentifier(userPermissionDto.getObjectIdentifier());
//                                    if (userPermissionOptional.isPresent()) {
//                                        UserPermission userPermission = userPermissionOptional.get();
//                                        userPermission.setSourceApplicationIds(userPermissionDto.getSourceApplicationIds());
//                                        return userPermissionRepository.save(userPermission);
//                                    } else {
//                                        UserPermission userPermission = UserPermission
//                                                .builder()
//                                                .objectIdentifier(userPermissionDto.getObjectIdentifier())
//                                                .sourceApplicationIds(userPermissionDto.getSourceApplicationIds())
//                                                .build();
//                                        return userPermissionRepository.save(userPermission);
//                                    }
//                                }).subscribeOn(Schedulers.boundedElastic())
//                                .collectList()
//                                .map(ResponseEntity::ok);
//                    } else {
//                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
//                    }
//
//                });
//    }

    @PostMapping("userpermissions")
    public Mono<ResponseEntity<List<UserPermissionDto>>> setUserPermission(
            @RequestBody List<UserPermissionDto> userPermissionDtos,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Flux.fromIterable(userPermissionDtos)
                                .flatMap(userPermissionDto -> Mono.fromCallable(() -> {
                                    Optional<UserPermission> userPermissionOptional = userPermissionRepository
                                            .findByObjectIdentifier(userPermissionDto.getObjectIdentifier());
                                    if (userPermissionOptional.isPresent()) {
                                        UserPermission userPermission = userPermissionOptional.get();
                                        userPermission.setSourceApplicationIds(userPermissionDto.getSourceApplicationIds());
                                        userPermissionRepository.save(userPermission);

                                        return UserPermissionDto
                                                .builder()
                                                .objectIdentifier(userPermission.getObjectIdentifier())
                                                .sourceApplicationIds(userPermission.getSourceApplicationIds())
                                                .build();
                                    } else {
                                        UserPermission newUserPermission = UserPermission.builder()
                                                .objectIdentifier(userPermissionDto.getObjectIdentifier())
                                                .sourceApplicationIds(userPermissionDto.getSourceApplicationIds())
                                                .build();
                                        userPermissionRepository.save(newUserPermission);

                                        return UserPermissionDto
                                                .builder()
                                                .objectIdentifier(newUserPermission.getObjectIdentifier())
                                                .sourceApplicationIds(newUserPermission.getSourceApplicationIds())
                                                .build();
                                    }
                                }).subscribeOn(Schedulers.boundedElastic()))
                                .collectList()
                                .map(ResponseEntity::ok);
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                });
    }


    private Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }

}
