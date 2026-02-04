package no.novari.flyt.authorization.user.controller;

import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication;
import no.novari.flyt.authorization.user.UserService;
import no.novari.flyt.authorization.user.controller.utils.TokenParsingUtils;
import no.novari.flyt.authorization.user.model.RestrictedPageAuthorization;
import no.novari.flyt.authorization.user.model.User;
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

import static no.novari.flyt.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/me")
public class MeController {

    private final TokenParsingUtils tokenParsingUtils;
    private final UserService userService;
    private final List<SourceApplication> sourceApplications;

    public MeController(
            TokenParsingUtils tokenParsingUtils,
            UserService userService,
            List<SourceApplication> sourceApplications
    ) {
        this.tokenParsingUtils = tokenParsingUtils;
        this.userService = userService;
        this.sourceApplications = sourceApplications;
    }

    @GetMapping("is-authorized")
    public ResponseEntity<?> checkAuthorization(
            @AuthenticationPrincipal Authentication authentication
    ) {
        JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;
        Optional<User> userOptional = getUserFromUserAuthorizationComponent(jwtAuthToken);

        if (userOptional.isEmpty() && tokenParsingUtils.hasPermittedRole(jwtAuthToken)) {
            createUserFromToken(authentication);
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
        return sourceApplications.stream()
                .map(SourceApplication::getId)
                .sorted()
                .toList();
    }

}
