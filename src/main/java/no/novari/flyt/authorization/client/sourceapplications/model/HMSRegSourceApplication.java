package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HMSRegSourceApplication extends BaseSourceApplication {
    public HMSRegSourceApplication(
            @Value("${fint.flyt.hmsreg.sso.client-id:#{null}}") String clientId,
            @Value("${fint.flyt.hmsreg.available:true}") boolean available
    ) {
        super(6L, "HMSReg", clientId, available);
    }
}
