package no.fintlabs.flyt.models.sourceapplication;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class VigoSourceApplication {
    public static String CLIENT_ID;
    public static final String SOURCE_APPLICATION_ID = "4";

    @Value("${fint.flyt.vigo.sso.client-id:#{null}}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }
}
