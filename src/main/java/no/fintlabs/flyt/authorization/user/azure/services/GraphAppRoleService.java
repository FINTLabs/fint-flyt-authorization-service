package no.fintlabs.flyt.authorization.user.azure.services;

import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


@Service
@Slf4j
public class GraphAppRoleService {

    private final GraphServiceClient<Request> graphServiceClient;

    private final GraphPageWalkerService graphPageWalkerService;

    public GraphAppRoleService(
            GraphServiceClient<Request> graphServiceClient,
            GraphPageWalkerService graphPageWalkerService
    ) {
        this.graphServiceClient = graphServiceClient;
        this.graphPageWalkerService = graphPageWalkerService;
    }

    public Set<UUID> getAppRoleIdsFromAppRoleValues(UUID servicePrincipalId, Set<String> appRoleValues) {
        log.info("Retrieving app role ids from app role values: {}", appRoleValues);
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

        Set<UUID> appRoleIds = appRoleIdPerValue.values().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        log.info("Successfully retrieved {} app role ids: {}", appRoleIds.size(), appRoleIds);
        return appRoleIds;
    }

    @Getter
    @AllArgsConstructor
    public static class AppRoleIdAndPrincipalSelection {
        private final UUID appRoleId;
        private final String principalType;
        private final UUID principalId;
    }

    public List<AppRoleIdAndPrincipalSelection> getAppRoleAssignmentsWithAppRoleIds(UUID servicePrincipalId, Set<UUID> appRoleIds) {
        log.info("Retrieving app role assignments with app role ids: {}", appRoleIds);
        List<AppRoleIdAndPrincipalSelection> appRoleAssignments = graphPageWalkerService.getContentFromCurrentAndNextPages(
                        graphServiceClient
                                .servicePrincipals(servicePrincipalId.toString())
                                .appRoleAssignedTo()
                                .buildRequest()
                                .select("appRoleId,principalType,principalId"),
                        pageContent -> pageContent.stream()
                                .filter(appRoleAssignment -> appRoleIds.contains(appRoleAssignment.appRoleId))
                                .toList()
                )
                .stream()
                .map(this::fromAppRoleAssignment)
                .toList();
        log.info("Successfully retrieved {} app role assignments: {}", appRoleAssignments.size(), appRoleAssignments);
        return appRoleAssignments;
    }

    private AppRoleIdAndPrincipalSelection fromAppRoleAssignment(AppRoleAssignment appRoleAssignment) {
        return new AppRoleIdAndPrincipalSelection(
                appRoleAssignment.appRoleId,
                appRoleAssignment.principalType,
                appRoleAssignment.principalId
        );
    }

}
