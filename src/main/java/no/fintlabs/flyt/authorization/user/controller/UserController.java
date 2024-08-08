package no.fintlabs.flyt.authorization.user.controller;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@ConditionalOnProperty(value = "fint.flyt.authorization.access-control.enabled", havingValue = "true")
@RestController
@RequestMapping(INTERNAL_API + "/authorization/users")
@RequiredArgsConstructor
public class UserController {

    private final TokenParsingUtils tokenParsingUtils;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<User>> get(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        if (!tokenParsingUtils.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<User> users = userService.getAll(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("actions/userPermissionBatchPut")
    public ResponseEntity<?> postUserPermissionBatchPutAction(
            @AuthenticationPrincipal Authentication authentication,
            @RequestBody List<User> users
    ) {
        if (!tokenParsingUtils.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.putAll(users);
        userService.publishUsers();
        return ResponseEntity.ok().build();
    }

}
