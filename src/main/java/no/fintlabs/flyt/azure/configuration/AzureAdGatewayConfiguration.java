package no.fintlabs.flyt.azure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;


@Getter
@Setter
@Validated
@EnableAutoConfiguration
@Configuration
@ConfigurationProperties(prefix = "fint.flyt.azure-ad-gateway")
public class AzureAdGatewayConfiguration {

    @Valid
    private PermittedAppRoles permittedAppRoles;

}
