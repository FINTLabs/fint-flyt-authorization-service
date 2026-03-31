package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class IsyGravingSourceApplication(
    @Value("\${fint.flyt.isygraving.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.isygraving.available:false}")
    available: Boolean,
) : BaseSourceApplication(
        id = 7L,
        displayName = "ISY Graving",
        clientId = clientId,
        available = available,
    )
