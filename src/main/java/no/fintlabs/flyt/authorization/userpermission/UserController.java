package no.fintlabs.flyt.authorization.userpermission;

import no.fintlabs.flyt.authorization.AuthorizationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/users")
public class UserController {

    private final AuthorizationUtil authorizationUtil;
    private final UserService userService;

    public UserController(
            AuthorizationUtil authorizationUtil, UserService userService
    ) {
        this.authorizationUtil = authorizationUtil;
        this.userService = userService;
    }

    @GetMapping
    public Mono<ResponseEntity<List<User>>> get(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authorizationUtil.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }

                    // todo: add pagable with sorting
                    return Mono.fromCallable(userService::getUsers)
                            .map(ResponseEntity::ok);

//                    return Mono.fromCallable(userPermissionRepository::findAll)
//                            .subscribeOn(Schedulers.boundedElastic())
//                            .map(userPermissions -> {
//                                List<User> users = new java.util.ArrayList<>(List.of());
//                                userPermissions.forEach(
//                                        userPermission -> users.add(
//                                                authorizationUtil.buildUserPermissionDto(userPermission)
//                                        )
//                                );
//                                users.sort(
//                                        Comparator.comparing(
//                                                User::getName, Comparator.nullsLast(String::compareTo)
//                                        )
//                                );
//                                return ResponseEntity.ok().body(users);
//                            });
                });
    }

//    @PutMapping("users")
//    public Mono<ResponseEntity<List<UserPermission>>> setUserPermissions(
//            @RequestBody List<UserPermission> userPermissions,
//            @AuthenticationPrincipal Mono<Authentication> authenticationMono
//    ) {
//        return authorizationUtil.isAdmin(authenticationMono)
//                .publishOn(Schedulers.boundedElastic())
//                .flatMap(isAdmin -> {
//                    if (isAdmin) {
//                        return Flux.fromIterable(userPermissions)
//                                .flatMap(userPermissionDto -> Mono.fromCallable(() -> {
//                                    Optional<UserPermissionEntity> userPermissionOptional = userPermissionRepository
//                                            .findByObjectIdentifier(userPermissionDto.getObjectIdentifier());
//                                    return userPermissionOptional.map(userPermission -> {
//                                        userPermission.setSourceApplicationIds(userPermissionDto.getSourceApplicationIds());
//                                        userPermissionRepository.save(userPermission);
//                                        return authorizationUtil.buildUserPermissionDto(userPermission);
//                                    }).orElse(null);
//                                }).subscribeOn(Schedulers.boundedElastic()))
//                                .collectList()
//                                .map(userPermissionDtoList -> {
//                                    userPermissionDtoList.sort(
//                                            Comparator.comparing(
//                                                    UserPermission::getName, Comparator.nullsLast(String::compareTo)
//                                            )
//                                    );
//                                    return ResponseEntity.ok().body(userPermissionDtoList);
//                                });
//                    } else {
//                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
//                    }
//                });
//    }


}
