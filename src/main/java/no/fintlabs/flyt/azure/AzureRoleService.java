package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.*;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AzureRoleService {

    protected final GraphServiceClient<Request> graphService;

    public AzureRoleService(GraphServiceClient<Request> graphService) {
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

    public String getRoleName(UUID appRoleId, String appId) {
        List<AppRole> appRoles = getAppRoles(appId);
        if (appRoles == null) {
            log.warn("App roles are null for appId: {}", appId);
            return null;
        }
        for (AppRole appRole : appRoles) {
            if (appRole.id != null && appRole.id.equals(appRoleId)) {
                return appRole.value;
            }
        }
        log.warn("Role with appRoleId: {} not found in appId: {}", appRoleId, appId);
        return null;
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

    public List<String> getUserRoles(String userId, String email, String appId) {
        List<AppRoleAssignment> appRoleAssignments = getAppRoleAssignments(appId);
        List<String> roles = new ArrayList<>();

        // Check direct user role assignments
        for (AppRoleAssignment assignment : appRoleAssignments) {

            assert assignment.principalType != null;
            assert assignment.principalId != null;
            String principalType = assignment.principalType;
            String principalId = assignment.principalId.toString();

            if (principalType.equals("User") && principalId.equals(userId)) {
                String roleName = getRoleName(assignment.appRoleId, appId);
                if (roleName != null) {
                    roles.add(roleName);
                } else {
                    log.warn("Role name for appRoleId {} not found", assignment.appRoleId);
                }
            }
        }

        // Check if user is in any group that has specific roles
        List<User> groupMembers;
        for (AppRoleAssignment assignment : appRoleAssignments) {
            if (assignment.principalType != null && assignment.principalType.equals("Group")) {
                groupMembers = getGroupMembers(String.valueOf(assignment.principalId));
                for (User user : groupMembers) {
                    if (user.id != null && user.id.equals(userId)) {
                        String roleName = getRoleName(assignment.appRoleId, appId);
                        if (roleName != null) {
                            roles.add(roleName);
                        }
                    }
                }
            }
        }

        // Log the roles
        if (roles.isEmpty()) {
            log.info("User {} has no roles assigned in app {}", email, appId);
            return List.of();
        } else {
            log.info("User {} has the following roles in app {}: {}", email, appId, roles);
            return roles;
        }
    }
}
