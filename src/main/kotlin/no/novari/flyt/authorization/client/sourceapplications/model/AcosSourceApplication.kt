package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AcosSourceApplication(
    @Value("\${fint.flyt.acos.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.acos.available:false}")
    available: Boolean,
) : BaseSourceApplication(
        id = 1L,
        displayName = "Acos Interact",
        clientId = clientId,
        available = available,
    )
