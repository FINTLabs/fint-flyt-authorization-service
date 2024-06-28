package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.user.AzureAdUserAuthorizationComponent;
import no.fintlabs.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.fintlabs.flyt.authorization.user.model.User;
import no.fintlabs.flyt.authorization.user.model.UserPermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    public Mono<ResponseEntity<List<User>>> get(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return tokenParsingUtils.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }

                    // TODO: add pagable with sorting
                    return Mono.fromCallable(azureAdUserAuthorizationComponent::getUsers)
                            .map(ResponseEntity::ok);
                });
    }

    @PutMapping
    public Mono<ResponseEntity<List<User>>> setUserPermissions(
            @RequestBody List<UserPermission> userPermissions,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return tokenParsingUtils.isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }

                    // TODO: add pagable with sorting
                    return Mono.just(ResponseEntity.ok(azureAdUserAuthorizationComponent.putUsers(userPermissions)));
                });
    }
}