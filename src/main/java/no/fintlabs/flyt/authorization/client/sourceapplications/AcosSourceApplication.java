package no.fintlabs.flyt.authorization.client.sourceapplications;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AcosSourceApplication {
    public static String CLIENT_ID;
    public static final long SOURCE_APPLICATION_ID = 1L;

    @Value("${fint.flyt.acos.sso.client-id:#{null}}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }
}
