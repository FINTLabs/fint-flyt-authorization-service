package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.*;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AzureAppRoleService {

    protected final GraphServiceClient<Request> graphService;

    public AzureAppRoleService(
            GraphServiceClient<Request> graphService
    ) {
        this.graphService = graphService;
    }

    public List<AppRoleAssignment> getAppRoleAssignments(String appId) {
        List<QueryOption> requestOptions = new ArrayList<>();
        requestOptions.add(new QueryOption("$filter", "appId eq '" + appId + "'"));
        List<ServicePrincipal> servicePrincipals = Objects.requireNonNull(graphService
                        .servicePrincipals()
                        .buildRequest(requestOptions)
                        .get())
                .getCurrentPage();
        if (!servicePrincipals.isEmpty()) {
            ServicePrincipal servicePrincipal = servicePrincipals.get(0);
            if (servicePrincipal != null && servicePrincipal.id != null) {
                return Objects.requireNonNull(graphService
                                .servicePrincipals(servicePrincipal.id)
                                .appRoleAssignedTo()
                                .buildRequest()
                                .get())
                        .getCurrentPage();
            }
        }
        return new ArrayList<>();
    }

    public List<AppRole> getAppRoles(String appId) {
        List<QueryOption> requestOptions = new ArrayList<>();
        requestOptions.add(new QueryOption("$filter", "appId eq '" + appId + "'"));
        List<ServicePrincipal> servicePrincipals = Objects.requireNonNull(graphService
                        .servicePrincipals()
                        .buildRequest(requestOptions)
                        .get())
                .getCurrentPage();
        if (!servicePrincipals.isEmpty()) {
            ServicePrincipal servicePrincipal = servicePrincipals.get(0);
            if (servicePrincipal != null && servicePrincipal.id != null) {
                return servicePrincipal.appRoles;
            }
        }
        return new ArrayList<>();
    }

    public List<User> getGroupMembers(String groupId) {
        List<DirectoryObject> members = Objects.requireNonNull(graphService
                        .groups(groupId)
                        .members()
                        .buildRequest()
                        .get())
                .getCurrentPage();

        return members.stream()
                .filter(directoryObject -> directoryObject instanceof User)
                .map(directoryObject -> (User) directoryObject)
                .collect(Collectors.toList());
    }

}
