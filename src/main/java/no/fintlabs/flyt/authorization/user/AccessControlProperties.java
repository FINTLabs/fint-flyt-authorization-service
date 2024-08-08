package no.fintlabs.flyt.authorization.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fint.flyt.authorization.access-control")
public class AccessControlProperties {
    private Map<String, String> permittedAppRoles;
}
