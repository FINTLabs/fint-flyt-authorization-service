package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FSkyssSourceApplication(
    @Value("\${fint.flyt.fskyss.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.fskyss.available:false}")
    available: Boolean,
) : BaseSourceApplication(
        id = 8L,
        displayName = "FSkyss",
        clientId = clientId,
        available = available,
    )
