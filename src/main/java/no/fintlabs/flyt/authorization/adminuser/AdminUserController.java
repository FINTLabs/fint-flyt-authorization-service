package no.fintlabs.flyt.authorization.adminuser;

import no.fintlabs.flyt.authorization.AuthorizationUtil;
import no.fintlabs.flyt.authorization.user.UserPermission;
import no.fintlabs.flyt.authorization.user.UserPermissionDto;
import no.fintlabs.flyt.authorization.user.UserPermissionRepository;
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
@RequestMapping(INTERNAL_API + "/admin/authorization")
public class AdminUserController {

    private final UserPermissionRepository userPermissionRepository;
    private final AuthorizationUtil authorizationUtil;

    public AdminUserController(
            UserPermissionRepository userPermissionRepository,
            AuthorizationUtil authorizationUtil
    ) {
        this.userPermissionRepository = userPermissionRepository;
        this.authorizationUtil = authorizationUtil;
    }

    @GetMapping("users")
    public Mono<ResponseEntity<List<UserPermissionDto>>> getUserPermissions(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authorizationUtil.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Mono.fromCallable(userPermissionRepository::findAll)
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(userPermissions -> {
                                    List<UserPermissionDto> userPermissionDtos = new java.util.ArrayList<>(List.of());
                                    userPermissions.forEach(
                                            userPermission -> userPermissionDtos.add(
                                                    authorizationUtil.buildUserPermissionDto(userPermission)
                                            )
                                    );
                                    userPermissionDtos.sort(
                                            Comparator.comparing(
                                                    UserPermissionDto::getName, Comparator.nullsLast(String::compareTo)
                                            )
                                    );
                                    return ResponseEntity.ok().body(userPermissionDtos);
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                });
    }

    @PutMapping("users")
    public Mono<ResponseEntity<List<UserPermissionDto>>> setUserPermissions(
            @RequestBody List<UserPermissionDto> userPermissionDtos,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authorizationUtil.isAdmin(authenticationMono)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Flux.fromIterable(userPermissionDtos)
                                .flatMap(userPermissionDto -> Mono.fromCallable(() -> {
                                    Optional<UserPermission> userPermissionOptional = userPermissionRepository
                                            .findByObjectIdentifier(userPermissionDto.getObjectIdentifier());
                                    return userPermissionOptional.map(userPermission -> {
                                        userPermission.setSourceApplicationIds(userPermissionDto.getSourceApplicationIds());
                                        userPermissionRepository.save(userPermission);
                                        return authorizationUtil.buildUserPermissionDto(userPermission);
                                    }).orElse(null);
                                }).subscribeOn(Schedulers.boundedElastic()))
                                .collectList()
                                .map(userPermissionDtoList -> {
                                    userPermissionDtoList.sort(
                                            Comparator.comparing(
                                                    UserPermissionDto::getName, Comparator.nullsLast(String::compareTo)
                                            )
                                    );
                                    return ResponseEntity.ok().body(userPermissionDtoList);
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                });
    }


}
