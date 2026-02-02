package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EgrunnervervSourceApplication extends BaseSourceApplication {
    public EgrunnervervSourceApplication(@Value("${fint.flyt.egrunnerverv.sso.client-id:#{null}}") String clientId) {
        super(2L, "eGrunnerverv", clientId);
    }
}
