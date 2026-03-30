package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EgrunnervervSourceApplication(
    @Value("\${fint.flyt.egrunnerverv.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.egrunnerverv.available:true}")
    available: Boolean,
) : BaseSourceApplication(
        id = 2L,
        displayName = "eGrunnerverv",
        clientId = clientId,
        available = available,
    )
