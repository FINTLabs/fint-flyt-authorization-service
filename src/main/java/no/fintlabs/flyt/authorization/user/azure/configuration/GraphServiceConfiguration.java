package no.fintlabs.flyt.authorization.user.azure.configuration;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;


@Getter
@Setter
@Validated
@EnableAutoConfiguration
@Configuration
public class GraphServiceConfiguration {

    @Bean
    @ConditionalOnBean(AzureCredentialsConfiguration.class)
//    @Conditional(RequiredPropertiesCondition.class)
    public GraphServiceClient<Request> graphServiceClient(AzureCredentialsConfiguration azureCredentialsConfiguration) {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(azureCredentialsConfiguration.getClientId())
                .clientSecret(azureCredentialsConfiguration.getClientSecret())
                .tenantId(azureCredentialsConfiguration.getTenantId())
                .build();

        TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(
                List.of("https://graph.microsoft.com/.default"),
                clientSecretCredential
        );

        return GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredentialAuthProvider)
                .buildClient();
    }

}
