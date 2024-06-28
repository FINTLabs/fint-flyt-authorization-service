package no.fintlabs.flyt.authorization.user.azure.services;

import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ServicePrincipalCollectionPage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

import java.util.UUID;

@Slf4j
public class GraphServicePrincipalService {
    // TODO eivindmorch 27/06/2024 : Rename service?
    private final GraphServiceClient<Request> graphServiceClient;

    public GraphServicePrincipalService(GraphServiceClient<Request> graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    // TODO eivindmorch 27/06/2024 : Remove?
    public UUID getServicePrincipalId(UUID appId) {
        log.info("Retrieving service principal id for application with id: {}", appId);
        ServicePrincipalCollectionPage servicePrincipalSearchResult = graphServiceClient
                .servicePrincipals()
                .buildRequest(new QueryOption("$filter", "appId eq '" + appId.toString() + "'"))
                .get();// TODO eivindmorch 27/06/2024 : Select only needed data

        if (servicePrincipalSearchResult == null || servicePrincipalSearchResult.getCurrentPage().isEmpty()) {
            throw new IllegalStateException("No principal for application found");
        }
        if (servicePrincipalSearchResult.getCurrentPage().size() > 1 || servicePrincipalSearchResult.getNextPage() != null) {
            throw new IllegalStateException("Found multiple service principals for application");
        }
        String servicePrincipalIdString = servicePrincipalSearchResult.getCurrentPage().get(0).id;
        if (servicePrincipalIdString == null) {
            throw new IllegalStateException("Service principal id is null");
        }

        UUID servicePrincipalId = UUID.fromString(servicePrincipalIdString);
        log.info("Successfully retrieved service principal id: {}", servicePrincipalId);
        return servicePrincipalId;
    }

}
