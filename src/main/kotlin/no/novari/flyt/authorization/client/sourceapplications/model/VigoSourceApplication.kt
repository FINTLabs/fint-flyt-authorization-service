package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VigoSourceApplication(
    @Value("\${fint.flyt.vigo.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.vigo.available:true}")
    available: Boolean,
) : BaseSourceApplication(
        id = 4L,
        displayName = "VIGO",
        clientId = clientId,
        available = available,
    )
