package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.client.sourceapplications.AcosSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.DigisakSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.EgrunnervervSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.VigoSourceApplication;
import no.fintlabs.flyt.authorization.user.UserAuthorizationComponent;
import no.fintlabs.flyt.authorization.user.azure.models.UserDisplayText;
import no.fintlabs.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.fintlabs.flyt.authorization.user.model.RestrictedPageAuthorization;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RequestMapping(INTERNAL_API + "/authorization/me")
@RestController
public class MeController {

    private final TokenParsingUtils tokenParsingUtils;

    private final UserAuthorizationComponent userAuthorizationComponent;

    public MeController(
            TokenParsingUtils tokenParsingUtils,
            @Autowired(required = false) UserAuthorizationComponent userAuthorizationComponent
    ) {
        this.tokenParsingUtils = tokenParsingUtils;
        this.userAuthorizationComponent = userAuthorizationComponent;
    }

    @GetMapping("is-authorized")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("restricted-page-authorization")
    public Mono<ResponseEntity<RestrictedPageAuthorization>> getRestrictedPageAuthorization(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return (userAuthorizationComponent == null
                ? Mono.just(RestrictedPageAuthorization.builder().build())
                : tokenParsingUtils.isAdmin(authenticationMono)
                .map(isAdmin -> RestrictedPageAuthorization
                        .builder()
                        .userPermissionPage(isAdmin)
                        .build()
                ))
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<User>> getSourceApplicationIds(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono
                .map(authentication -> (JwtAuthenticationToken) authentication)
                .map(authentication ->
                        userAuthorizationComponent == null
                                ? ResponseEntity.ok(createUserWithFullAccessFromToken(authentication))
                                : getUserFromUserAuthorizationComponent(authentication)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build())
                );
    }

    private User createUserWithFullAccessFromToken(JwtAuthenticationToken token) {
        UserDisplayText userDisplayTextFromToken =
                tokenParsingUtils.getUserDisplayTextFromToken(token);
        return User.builder()
                .objectIdentifier(userDisplayTextFromToken.getObjectIdentifier())
                .name(userDisplayTextFromToken.getName())
                .email(userDisplayTextFromToken.getEmail())
                .sourceApplicationIds(sourceApplicationsWithoutUserPermissionSetup())
                .build();
    }

    private Optional<User> getUserFromUserAuthorizationComponent(JwtAuthenticationToken token) {
        return userAuthorizationComponent.getUser(
                UUID.fromString(tokenParsingUtils.getObjectIdentifierFromToken(token))
        );
    }

    private List<Long> sourceApplicationsWithoutUserPermissionSetup() {
        return List.of(
                AcosSourceApplication.SOURCE_APPLICATION_ID,
                DigisakSourceApplication.SOURCE_APPLICATION_ID,
                EgrunnervervSourceApplication.SOURCE_APPLICATION_ID,
                VigoSourceApplication.SOURCE_APPLICATION_ID
        );
    }

}
