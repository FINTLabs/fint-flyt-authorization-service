package no.novari.flyt.authorization.client.sourceapplications.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ConexusSourceApplication(
    @Value("\${fint.flyt.conexus.sso.client-id:#{null}}")
    clientId: String?,
    @Value("\${fint.flyt.conexus.available:false}")
    available: Boolean,
) : BaseSourceApplication(
        id = 11L,
        displayName = "Conexus",
        clientId = clientId,
        available = available,
    )
