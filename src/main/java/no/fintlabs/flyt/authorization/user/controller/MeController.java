package no.fintlabs.flyt.authorization.user.controller;

import no.fintlabs.flyt.authorization.client.sourceapplications.AcosSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.DigisakSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.EgrunnervervSourceApplication;
import no.fintlabs.flyt.authorization.client.sourceapplications.VigoSourceApplication;
import no.fintlabs.flyt.authorization.user.UserService;
import no.fintlabs.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.fintlabs.flyt.authorization.user.model.RestrictedPageAuthorization;
import no.fintlabs.flyt.authorization.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/me")
public class MeController {

    private final TokenParsingUtils tokenParsingUtils;
    private final Boolean accessControlEnabled;
    private final UserService userService;

    public MeController(
            TokenParsingUtils tokenParsingUtils,
            @Value("${fint.flyt.authorization.access-control.enabled}") Boolean accessControlEnabled,
            UserService userService
    ) {
        this.tokenParsingUtils = tokenParsingUtils;
        this.accessControlEnabled = accessControlEnabled;
        this.userService = userService;
    }

    @GetMapping("is-authorized")
    public ResponseEntity<?> checkAuthorization(
            @AuthenticationPrincipal Authentication authentication
    ) {
        if (accessControlEnabled) {
            JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;
            Optional<User> userOptional = getUserFromUserAuthorizationComponent(jwtAuthToken);

            if (userOptional.isEmpty() && tokenParsingUtils.hasPermittedRole(jwtAuthToken)) {
                createUserFromToken(authentication);
            }
        }
        return ResponseEntity.ok("User authorized");
    }

    @GetMapping("restricted-page-authorization")
    public ResponseEntity<RestrictedPageAuthorization> getRestrictedPageAuthorization(
            @AuthenticationPrincipal Authentication authentication
    ) {
        boolean isAdmin = tokenParsingUtils.isAdmin(authentication);
        RestrictedPageAuthorization authorization = RestrictedPageAuthorization
                .builder()
                .userPermissionPage(isAdmin)
                .build();
        return ResponseEntity.ok(authorization);
    }

    @GetMapping
    public ResponseEntity<User> get(
            @AuthenticationPrincipal Authentication authentication
    ) {
        JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;
        Optional<User> userOptional = getUserFromUserAuthorizationComponent(jwtAuthToken);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            User newUser = createUserFromToken(authentication);
            return ResponseEntity.ok(newUser);
        }
    }

    private Optional<User> getUserFromUserAuthorizationComponent(JwtAuthenticationToken token) {
        return userService.find(UUID.fromString(tokenParsingUtils.getObjectIdentifierFromToken(token)));
    }

    private User createUserFromToken(Authentication authentication) {
        JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;
        User.UserBuilder userBuilder = tokenParsingUtils.getUserFromToken(jwtAuthToken).toBuilder();
        boolean isAdmin = tokenParsingUtils.isAdmin(authentication);
        if (isAdmin) {
            userBuilder.sourceApplicationIds(allSourceApplicationIds());
        }
        User newUser = userBuilder.build();
        userService.save(newUser);
        return newUser;
    }

    private List<Long> allSourceApplicationIds() {
        return List.of(
                AcosSourceApplication.SOURCE_APPLICATION_ID,
                DigisakSourceApplication.SOURCE_APPLICATION_ID,
                EgrunnervervSourceApplication.SOURCE_APPLICATION_ID,
                VigoSourceApplication.SOURCE_APPLICATION_ID
        );
    }

}
