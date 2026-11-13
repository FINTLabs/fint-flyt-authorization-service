package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import org.springframework.stereotype.Service

@Service
class ClientAuthorizationService(
    private val sourceApplications: List<SourceApplication>,
) {
    fun find(clientId: String): ClientAuthorization? =
        sourceApplications
            .firstOrNull { it.clientId == clientId }
            ?.let { ClientAuthorization(authorized = true, clientId = clientId, sourceApplicationId = it.id) }
}
