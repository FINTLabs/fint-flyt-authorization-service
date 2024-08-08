package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.user.UserPublishingComponent;
import no.fintlabs.flyt.authorization.user.UserService;
import no.fintlabs.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@ConditionalOnProperty(value = "fint.flyt.authorization.access-control.enabled", havingValue = "true")
@RestController
@RequestMapping(INTERNAL_API + "/authorization/users")
public class UserController {

    private final TokenParsingUtils tokenParsingUtils;
    private final UserPublishingComponent userPublishingComponent;
    private final UserService userService;

    public UserController(
            TokenParsingUtils tokenParsingUtils,
            UserPublishingComponent userPublishingComponent, UserService userService
    ) {
        this.tokenParsingUtils = tokenParsingUtils;
        this.userPublishingComponent = userPublishingComponent;
        this.userService = userService;
    }

    @GetMapping
    public Mono<ResponseEntity<Page<User>>> get(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return tokenParsingUtils.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return Mono.fromCallable(
                            () -> userPublishingComponent.getUsers(pageable)
                    ).map(ResponseEntity::ok);
                });
    }

    @PostMapping("actions/userPermissionBatchPut")
    public Mono<ResponseEntity<?>> postUserPermissionBatchPutAction(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @RequestBody List<User> users
    ) {
        return tokenParsingUtils.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    userPublishingComponent.batchPutUserPermissions(users);
                    userService.publishUsers();
                    return Mono.just(ResponseEntity.ok().build());
                });
    }

}
