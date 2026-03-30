package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class HMSRegSourceApplication(
    @Value("\${fint.flyt.hmsreg.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.hmsreg.available:true}")
    available: Boolean,
) : BaseSourceApplication(
        id = 6L,
        displayName = "HMSReg",
        clientId = clientId,
        available = available,
    )
