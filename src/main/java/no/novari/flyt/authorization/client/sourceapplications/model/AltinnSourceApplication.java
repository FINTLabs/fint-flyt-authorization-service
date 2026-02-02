package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AltinnSourceApplication extends BaseSourceApplication {
    public AltinnSourceApplication(@Value("${fint.flyt.altinn.sso.client-id:#{null}}") String clientId) {
        super(5L, "Altinn", clientId);
    }
}
