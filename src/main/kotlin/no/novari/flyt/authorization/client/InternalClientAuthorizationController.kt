package no.novari.flyt.authorization.client

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_CLIENT_API
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$INTERNAL_CLIENT_API/authorization/clients")
@Tag(name = "Client authorization", description = "Source application authorization for trusted service clients.")
class InternalClientAuthorizationController(
    private val clientAuthorizationService: ClientAuthorizationService,
) {
    @GetMapping("/{clientId}")
    @Operation(summary = "Get the source application authorization for a client")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Client is authorized for a source application"),
            ApiResponse(responseCode = "404", description = "Client is not registered for any source application"),
        ],
    )
    fun get(
        @Parameter(description = "The OAuth client ID of the source application")
        @PathVariable clientId: String,
    ): ClientAuthorization =
        clientAuthorizationService.find(clientId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
