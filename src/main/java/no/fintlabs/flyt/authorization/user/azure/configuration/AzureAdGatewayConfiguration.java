package no.fintlabs.flyt.authorization.user.azure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@Validated
@EnableAutoConfiguration
@Configuration
@ConfigurationProperties(prefix = "fint.flyt.azure-ad-gateway")
public class AzureAdGatewayConfiguration {

    @NotNull
    private Boolean enable;

    @Valid
    private PermittedAppRolesProperties permittedAppRolesProperties;

    @Getter
    @Setter
    public static class PermittedAppRolesProperties {
        @NotEmpty
        private String flytUser;
    }

}
