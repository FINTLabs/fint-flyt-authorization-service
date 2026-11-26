package no.novari.flyt.authorization.client

import no.novari.flyt.authorization.client.sourceapplications.SourceApplications
import org.springframework.stereotype.Service

@Service
class ClientAuthorizationService(
    private val sourceApplications: SourceApplications,
) {
    fun find(clientId: String): ClientAuthorization? =
        sourceApplications.all
            .firstOrNull { it.clientId == clientId }
            ?.let { ClientAuthorization(authorized = true, clientId = clientId, sourceApplicationId = it.id) }
}
