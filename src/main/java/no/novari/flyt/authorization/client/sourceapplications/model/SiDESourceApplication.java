package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SiDESourceApplication extends BaseSourceApplication {
    public SiDESourceApplication(
            @Value("${fint.flyt.side.sso.client-id:#{null}}") String clientId,
            @Value("${fint.flyt.side.available:false}") boolean available
    ) {
        super(9L, "SiDE", clientId, available);
    }
}
