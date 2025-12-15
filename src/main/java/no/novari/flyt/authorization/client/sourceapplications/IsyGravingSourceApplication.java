package no.novari.flyt.authorization.client.sourceapplications;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class IsyGravingSourceApplication {
    public static String CLIENT_ID;
    public static final long SOURCE_APPLICATION_ID = 7L;

    @Value("${fint.flyt.isygraving.sso.client-id:#{null}}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }
}
