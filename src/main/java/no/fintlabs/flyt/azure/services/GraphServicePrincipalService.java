package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.AppRoleAssignmentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ServicePrincipalCollectionPage;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GraphServicePrincipalService {
    // TODO eivindmorch 27/06/2024 : Rename service?
    private final GraphServiceClient<Request> graphServiceClient;

    public GraphServicePrincipalService(GraphServiceClient<Request> graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    // TODO eivindmorch 27/06/2024 : Remove?
    public UUID getServicePrincipalId(UUID appId) {
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
        String servicePrincipalId = servicePrincipalSearchResult.getCurrentPage().get(0).id;
        if (servicePrincipalId == null) {
            throw new IllegalStateException("Service principal id is null");
        }
        return UUID.fromString(servicePrincipalId);
    }

}
