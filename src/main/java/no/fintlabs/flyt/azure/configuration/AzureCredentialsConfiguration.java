package no.fintlabs.flyt.azure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@Validated
@EnableAutoConfiguration
@Configuration
@ConfigurationProperties(prefix = "azure.credentials")
public class AzureCredentialsConfiguration {
    @NotEmpty
    private String clientId;
    @NotEmpty
    private String clientSecret;
    @NotEmpty
    private String tenantId;
    @NotEmpty
    private String appId;
}
