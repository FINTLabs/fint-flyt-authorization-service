package no.fintlabs.models;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AcosSourceApplication {
    public static String CLIENT_ID;
    public static final String SOURCE_APPLICATION_ID = "1";

    @Value("${fint.flyt.acos.sso.client-id}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }
}
