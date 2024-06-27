package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.models.*;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ServicePrincipalCollectionPage;
import no.fintlabs.flyt.azure.models.wrappers.AppRoleAssignmentWrapper;
import no.fintlabs.flyt.azure.models.wrappers.GroupMembersWrapper;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AzureAppGraphService {

    protected final GraphServiceClient<Request> graphService;

    public AzureAppGraphService(
            GraphServiceClient<Request> graphService
    ) {
        this.graphService = graphService;
    }

    public AppRoleAssignmentWrapper getAppRoleAssignments(String appId) {
        List<QueryOption> requestOptions = new ArrayList<>();
        requestOptions.add(new QueryOption("$filter", "appId eq '" + appId + "'"));

        ServicePrincipalCollectionPage servicePrincipalCollectionPage = graphService
                .servicePrincipals()
                .buildRequest(requestOptions)
                .get();

        Objects.requireNonNull(servicePrincipalCollectionPage);

        List<ServicePrincipal> servicePrincipals = servicePrincipalCollectionPage.getCurrentPage();

        if (!servicePrincipals.isEmpty()) {
            ServicePrincipal servicePrincipal = servicePrincipals.get(0);
            if (servicePrincipal != null && servicePrincipal.id != null) {
                List<AppRoleAssignment> appRoleAssignments = Objects.requireNonNull(graphService
                                .servicePrincipals(servicePrincipal.id)
                                .appRoleAssignedTo()
                                .buildRequest()
                                .get())
                        .getCurrentPage();
                return new AppRoleAssignmentWrapper(appRoleAssignments);
            }
        }
        return new AppRoleAssignmentWrapper(new ArrayList<>());
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

    public GroupMembersWrapper getGroupMembers(String groupId) {
        List<DirectoryObject> members = Objects.requireNonNull(graphService
                        .groups(groupId)
                        .members()
                        .buildRequest()
                        .get())
                .getCurrentPage();

        List<User> users = members.stream()
                .filter(directoryObject -> directoryObject instanceof User)
                .map(directoryObject -> (User) directoryObject)
                .collect(Collectors.toList());

        return new GroupMembersWrapper(users);
    }

}
