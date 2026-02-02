package no.novari.flyt.authorization.client.sourceapplications.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IsyGravingSourceApplication extends BaseSourceApplication {
    public IsyGravingSourceApplication(@Value("${fint.flyt.isygraving.sso.client-id:#{null}}") String clientId) {
        super(7L, "ISY Graving", clientId);
    }
}
