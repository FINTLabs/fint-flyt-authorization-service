package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DigisakSourceApplication extends BaseSourceApplication {
    public DigisakSourceApplication(
            @Value("${fint.flyt.digisak.sso.client-id:#{null}}") String clientId,
            @Value("${fint.flyt.digisak.available:true}") boolean available
    ) {
        super(3L, "Digisak", clientId, available);
    }
}
