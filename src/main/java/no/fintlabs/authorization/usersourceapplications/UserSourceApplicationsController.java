package no.fintlabs.authorization.usersourceapplications;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RequestMapping(INTERNAL_API + "/authorization")
@RestController
public class UserSourceApplicationsController {

    @GetMapping("usersourceapplications")
    public ResponseEntity<UserSourceApplications> userSourceApplications() {
        return ResponseEntity.ok(
                UserSourceApplications
                        .builder()
                        .sourceApplicationIds(List.of(2))
                        .build()
        );
    }

}
