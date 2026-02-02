package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VigoSourceApplication extends BaseSourceApplication {
    public VigoSourceApplication(@Value("${fint.flyt.vigo.sso.client-id:#{null}}") String clientId) {
        super(4L, "VIGO", clientId);
    }
}
