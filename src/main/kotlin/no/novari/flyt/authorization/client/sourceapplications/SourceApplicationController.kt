package no.novari.flyt.authorization.client.sourceapplications

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$INTERNAL_API/authorization/sourceapplications")
@Tag(name = "Source applications", description = "Source applications available to Flyt users.")
class SourceApplicationController(
    private val sourceApplications: List<SourceApplication>,
) {
    @GetMapping
    @Operation(summary = "List source applications")
    fun getAll(): List<SourceApplicationResponse> {
        return sourceApplications
            .map { sourceApplication ->
                SourceApplicationResponse(
                    id = sourceApplication.id,
                    displayName = sourceApplication.displayName,
                    available = sourceApplication.available,
                )
            }.sortedBy { it.displayName.lowercase() }
    }

    data class SourceApplicationResponse(
        val id: Long,
        val displayName: String,
        val available: Boolean,
    )
}
