package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AltinnSourceApplication(
    @Value("\${fint.flyt.altinn.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.altinn.available:true}")
    available: Boolean,
) : BaseSourceApplication(
        id = 5L,
        displayName = "Altinn",
        clientId = clientId,
        available = available,
    )
