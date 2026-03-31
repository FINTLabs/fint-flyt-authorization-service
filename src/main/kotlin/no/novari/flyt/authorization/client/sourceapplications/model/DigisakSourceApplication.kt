package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DigisakSourceApplication(
    @Value("\${fint.flyt.digisak.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.digisak.available:false}")
    available: Boolean,
) : BaseSourceApplication(
        id = 3L,
        displayName = "Digisak",
        clientId = clientId,
        available = available,
    )
