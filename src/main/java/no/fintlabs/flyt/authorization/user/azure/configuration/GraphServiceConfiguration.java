package no.fintlabs.flyt.authorization.user.azure.configuration;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.Getter;
import lombok.Setter;
import no.fintlabs.flyt.authorization.user.azure.services.*;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;


@Getter
@Setter
@Validated
@EnableAutoConfiguration
@Configuration
@ConditionalOnProperty(value = "fint.flyt.azure-ad-gateway.enable", havingValue = "true")
public class GraphServiceConfiguration {

    @Bean
    public GraphService graphService(
            AzureCredentialsConfiguration azureCredentialsConfiguration,
            AzureAdGatewayConfiguration azureAdGatewayConfiguration,
            GraphServicePrincipalService graphServicePrincipalService,
            GraphAppRoleService graphAppRoleService,
            GraphGroupService graphGroupService,
            GraphUserService graphUserService
    ) {
        return new GraphService(
                azureCredentialsConfiguration,
                azureAdGatewayConfiguration,
                graphServicePrincipalService,
                graphAppRoleService,
                graphGroupService,
                graphUserService
        );
    }

    @Bean
    public GraphUserService graphUserService(
            GraphServiceClient<Request> graphServiceClient,
            GraphPageWalkerService graphPageWalkerService
    ) {
        return new GraphUserService(graphServiceClient, graphPageWalkerService);
    }

    @Bean
    public GraphAppRoleService graphAppRoleService(
            GraphServiceClient<Request> graphServiceClient,
            GraphPageWalkerService graphPageWalkerService
    ) {
        return new GraphAppRoleService(graphServiceClient, graphPageWalkerService);
    }

    @Bean
    public GraphGroupService graphGroupService(
            GraphServiceClient<Request> graphServiceClient,
            GraphPageWalkerService graphPageWalkerService
    ) {
        return new GraphGroupService(graphServiceClient, graphPageWalkerService);
    }

    @Bean
    public GraphServicePrincipalService graphServicePrincipalService(GraphServiceClient<Request> graphServiceClient) {
        return new GraphServicePrincipalService(graphServiceClient);
    }

    @Bean
    public GraphPageWalkerService graphPageWalkerService() {
        return new GraphPageWalkerService();
    }

    @Bean
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
