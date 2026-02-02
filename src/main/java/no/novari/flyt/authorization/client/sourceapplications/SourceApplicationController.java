package no.novari.flyt.authorization.client.sourceapplications;

import lombok.RequiredArgsConstructor;
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

import static no.novari.flyt.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/sourceapplications")
@RequiredArgsConstructor
public class SourceApplicationController {

    private final List<SourceApplication> sourceApplications;

    @GetMapping
    public ResponseEntity<List<SourceApplicationResponse>> getAll() {
        List<SourceApplicationResponse> response = sourceApplications.stream()
                .map(sourceApplication -> new SourceApplicationResponse(
                        sourceApplication.getSourceApplicationId(),
                        sourceApplication.getDisplayName()
                ))
                .sorted(Comparator.comparing(SourceApplicationResponse::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return ResponseEntity.ok(response);
    }

    public record SourceApplicationResponse(long sourceApplicationId, String displayName) {
    }
}
