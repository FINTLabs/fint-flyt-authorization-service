package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.client.sourceapplications.AcosSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.DigisakSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.EgrunnervervSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.VigoSourceApplication;
import no.fintlabs.flyt.authorization.user.AzureAdUserAuthorizationComponent;
import no.fintlabs.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.fintlabs.flyt.authorization.user.model.RestrictedPageAuthorization;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping(INTERNAL_API + "/authorization/me")
public class MeController {

    private final TokenParsingUtils tokenParsingUtils;

    private final Boolean azureAdUserAuthorizationEnabled;
    private final AzureAdUserAuthorizationComponent azureAdUserAuthorizationComponent;

    public MeController(
            TokenParsingUtils tokenParsingUtils,
            @Value("${fint.flyt.azure-ad-gateway.enabled}") Boolean azureAdUserAuthorizationEnabled,
            @Autowired(required = false) AzureAdUserAuthorizationComponent azureAdUserAuthorizationComponent
    ) {
        this.tokenParsingUtils = tokenParsingUtils;
        this.azureAdUserAuthorizationEnabled = azureAdUserAuthorizationEnabled;
        this.azureAdUserAuthorizationComponent = azureAdUserAuthorizationComponent;
    }

    @GetMapping("is-authorized")
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("restricted-page-authorization")
    public Mono<ResponseEntity<RestrictedPageAuthorization>> getRestrictedPageAuthorization(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return (azureAdUserAuthorizationEnabled
                ? tokenParsingUtils.isAdmin(authenticationMono)
                .map(isAdmin -> RestrictedPageAuthorization
                        .builder()
                        .userPermissionPage(isAdmin)
                        .build())
                : Mono.just(RestrictedPageAuthorization.builder().build())
        ).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<User>> get(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono
                .map(authentication -> (JwtAuthenticationToken) authentication)
                .map(authentication ->
                        azureAdUserAuthorizationEnabled
                                ? getUserFromUserAuthorizationComponent(authentication)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build())
                                : ResponseEntity.ok(createUserWithAccessToAllApplications(authentication))
                );
    }

    private Optional<User> getUserFromUserAuthorizationComponent(JwtAuthenticationToken token) {
        return azureAdUserAuthorizationComponent.getUser(
                UUID.fromString(tokenParsingUtils.getObjectIdentifierFromToken(token))
        );
    }

    private User createUserWithAccessToAllApplications(JwtAuthenticationToken token) {
        return tokenParsingUtils.getUserFromToken(token)
                .toBuilder()
                .sourceApplicationIds(sourceApplicationsWithoutUserPermissionSetup())
                .build();
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
