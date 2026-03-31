package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SiDESourceApplication(
    @Value("\${fint.flyt.side.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.side.available:false}")
    available: Boolean,
) : BaseSourceApplication(
        id = 9L,
        displayName = "SiDE",
        clientId = clientId,
        available = available,
    )
