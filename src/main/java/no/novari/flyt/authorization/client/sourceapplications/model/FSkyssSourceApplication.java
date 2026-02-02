package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FSkyssSourceApplication extends BaseSourceApplication {
    public FSkyssSourceApplication(
            @Value("${fint.flyt.fskyss.sso.client-id:#{null}}") String clientId,
            @Value("${fint.flyt.fskyss.available:false}") boolean available
    ) {
        super(8L, "FSkyss", clientId, available);
    }
}
