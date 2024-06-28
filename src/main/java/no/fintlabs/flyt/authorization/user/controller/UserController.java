package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.user.AzureAdUserAuthorizationComponent;
import no.fintlabs.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.model.UserPermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@ConditionalOnProperty(value = "fint.flyt.azure-ad-gateway.enable", havingValue = "true")
@RestController
@RequestMapping(INTERNAL_API + "/authorization/users")
public class UserController {

    private final TokenParsingUtils tokenParsingUtils;
    private final AzureAdUserAuthorizationComponent azureAdUserAuthorizationComponent;

    public UserController(
            TokenParsingUtils tokenParsingUtils,
            AzureAdUserAuthorizationComponent azureAdUserAuthorizationComponent
    ) {
        this.tokenParsingUtils = tokenParsingUtils;
        this.azureAdUserAuthorizationComponent = azureAdUserAuthorizationComponent;
    }

    @GetMapping
    public Mono<ResponseEntity<Page<User>>> get(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @RequestParam Pageable pageable
    ) {
        return tokenParsingUtils.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return Mono.fromCallable(
                            () -> azureAdUserAuthorizationComponent.getUsers(pageable)
                    ).map(ResponseEntity::ok);
                });
    }

    @PostMapping("actions/userPermissionBatchPut")
    public Mono<ResponseEntity<?>> postUserPermissionBatchPutAction(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @RequestBody List<UserPermission> userPermissions
    ) {
        return tokenParsingUtils.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    azureAdUserAuthorizationComponent.batchPutUserPermissions(userPermissions);
                    return Mono.just(ResponseEntity.ok().build());
                });
    }

}
