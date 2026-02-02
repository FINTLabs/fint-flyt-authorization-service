package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AcosSourceApplication extends BaseSourceApplication {
    public AcosSourceApplication(@Value("${fint.flyt.acos.sso.client-id:#{null}}") String clientId) {
        super(1L, "Acos Interact", clientId);
    }
}
