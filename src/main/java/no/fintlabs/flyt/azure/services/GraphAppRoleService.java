package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.requests.AppRoleAssignmentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


@Service
public class GraphAppRoleService {

    private final GraphServiceClient<Request> graphServiceClient;

    public GraphAppRoleService(GraphServiceClient<Request> graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    public Set<UUID> getAppRoleIdsFromAppRoleValues(UUID servicePrincipalId, Set<String> appRoleValues) {
        ServicePrincipal servicePrincipal = graphServiceClient.servicePrincipals(servicePrincipalId.toString())
                .buildRequest()
                .select("appRoles")
                .get();

        if (servicePrincipal == null) {
            throw new IllegalStateException("Service principal is null");
        }
        if (servicePrincipal.appRoles == null) {
            throw new IllegalStateException("Service principal has no roles");
        }

        Map<String, String> appRoleIdPerValue = servicePrincipal.appRoles
                .stream()
                .filter(appRole -> Objects.nonNull(appRole.id))
                .filter(appRole -> appRoleValues.contains(appRole.value))
                .collect(toMap(
                        appRole -> appRole.value,
                        appRole -> appRole.id.toString()
                ));

        Set<String> appRoleValuesThatCouldNotBeFound = new HashSet<>(appRoleValues);
        appRoleValuesThatCouldNotBeFound.removeAll(appRoleIdPerValue.keySet());
        if (!appRoleValuesThatCouldNotBeFound.isEmpty()) {
            throw new IllegalStateException("Could not find id for app roles with values: " + appRoleValuesThatCouldNotBeFound);
        }

        return appRoleIdPerValue.values().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @Getter
    @AllArgsConstructor
    public static class AppRoleIdAndPrincipalSelection {
        private final UUID appRoleId;
        private final String principalType;
        private final UUID principalId;
    }

    public List<AppRoleIdAndPrincipalSelection> getAppRoleAssignments(UUID servicePrincipalId) {
        AppRoleAssignmentCollectionPage appRoleAssignmentCollectionPage = graphServiceClient
                .servicePrincipals(servicePrincipalId.toString())
                .appRoleAssignedTo()
                .buildRequest()
                .select("appRoleId,principalType,principalId")
                .get();

        if (appRoleAssignmentCollectionPage == null) {
            throw new IllegalStateException("Could not retrieve app role assignments");
        }

        return appRoleAssignmentCollectionPage.getCurrentPage()
                .stream()
                .map(this::fromAppRoleAssignment)
                .toList();    // TODO eivindmorch 27/06/2024 : Handle multi page
    }

    private AppRoleIdAndPrincipalSelection fromAppRoleAssignment(AppRoleAssignment appRoleAssignment) {
        return new AppRoleIdAndPrincipalSelection(
                appRoleAssignment.appRoleId,
                appRoleAssignment.principalType,
                appRoleAssignment.principalId
        );
    }

}
