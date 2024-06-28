package no.fintlabs.flyt.authorization.user.azure.configuration;

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

    // TODO eivindmorch 28/06/2024 : Split properties?
    @Valid
    private PermittedAppRolesProperties permittedAppRolesProperties;

}
