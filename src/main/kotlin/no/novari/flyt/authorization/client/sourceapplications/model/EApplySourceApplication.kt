package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EApplySourceApplication(
    @Value("\${fint.flyt.eapply.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.eapply.available:true}")
    available: Boolean,
) : BaseSourceApplication(
        id = 10L,
        displayName = "eApply",
        clientId = clientId,
        available = available,
    )
