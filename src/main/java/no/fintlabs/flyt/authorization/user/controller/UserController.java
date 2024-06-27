package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.AuthorizationUtil;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.permission.model.UserPermission;
import no.fintlabs.flyt.authorization.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/users")
public class UserController {

    private final AuthorizationUtil authorizationUtil;
    private final UserService userService;

    public UserController(
            AuthorizationUtil authorizationUtil,
            UserService userService
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

                    // TODO: add pagable with sorting
                    return Mono.fromCallable(userService::getUsers)
                            .map(ResponseEntity::ok);
                });
    }

    @PutMapping
    public Mono<ResponseEntity<List<User>>> setUserPermissions(
            @RequestBody List<UserPermission> userPermissions,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authorizationUtil.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }

                    // TODO: add pagable with sorting
                    return Mono.just(ResponseEntity.ok(userService.putUsers(userPermissions)));
                });
    }
}