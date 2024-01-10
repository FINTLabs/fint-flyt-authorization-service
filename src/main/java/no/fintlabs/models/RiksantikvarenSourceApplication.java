package no.fintlabs.models;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RiksantikvarenSourceApplication {
    public static String CLIENT_ID;
    public static final String SOURCE_APPLICATION_ID = "3";

    @Value("${fint.flyt.riksantikvaren.sso.client-id:#{null}}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }
}
