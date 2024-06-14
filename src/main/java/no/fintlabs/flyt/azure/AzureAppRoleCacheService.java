package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.AppRole;
import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AzureAppRoleCacheService {

    protected final AzureAppRoleService azureAppRoleService;

    protected final AzureUserCacheRepository azureUserCacheRepository;
    protected final AzureAppRoleCacheRepository azureAppRoleCacheRepository;
    protected final AzureAppRoleAssignmentCacheRepository azureAppRoleAssignmentCacheRepository;
    protected final AzureGroupMembersCacheRepository azureGroupMembersCacheRepository;

    public AzureAppRoleCacheService(
            AzureAppRoleService azureAppRoleService,
            AzureUserCacheRepository azureUserCacheRepository,
            AzureAppRoleCacheRepository azureAppRoleCacheRepository,
            AzureAppRoleAssignmentCacheRepository azureAppRoleAssignmentCacheRepository,
            AzureGroupMembersCacheRepository azureGroupMembersCacheRepository) {
        this.azureAppRoleService = azureAppRoleService;
        this.azureUserCacheRepository = azureUserCacheRepository;
        this.azureAppRoleCacheRepository = azureAppRoleCacheRepository;
        this.azureAppRoleAssignmentCacheRepository = azureAppRoleAssignmentCacheRepository;
        this.azureGroupMembersCacheRepository = azureGroupMembersCacheRepository;
    }

    public void storeAzureAppRoleDataInCache(String appId) {
        List<AppRoleAssignment> appRoleAssignments = azureAppRoleService.getAppRoleAssignments(appId);
        azureAppRoleAssignmentCacheRepository.saveAll(appId, appRoleAssignments);

        appRoleAssignments.forEach(appRoleAssignment -> {
            if (appRoleAssignment.principalType == null || appRoleAssignment.principalId == null) {
                log.warn("Assignment principalType or principalId is null for appRoleAssignment {}", appRoleAssignment);
                return;
            }

            String principalType = appRoleAssignment.principalType;
            String principalId = appRoleAssignment.principalId.toString();

            if (principalType.equals("Group")) {
                List<User> groupMembers = azureAppRoleService.getGroupMembers(principalId);
                azureGroupMembersCacheRepository.saveAll(principalId, groupMembers);
            }
        });

        List<AppRole> appRoles = azureAppRoleService.getAppRoles(appId);
        azureAppRoleCacheRepository.saveAllPermittedRoles(appRoles);
    }

    public List<String> getUserRoles(
            String userId,
            String email,
            String appId
    ) {
        List<String> roles = new ArrayList<>();

        List<AppRoleAssignment> appRoleAssignments = this.azureAppRoleAssignmentCacheRepository.findAllByAppId(appId);

        appRoleAssignments.forEach(assignment -> {
            if (assignment.principalType == null || assignment.principalId == null) {
                log.debug("Assignment principalType or principalId is null for assignment {}", assignment);
                return;
            }

            String principalType = assignment.principalType;
            String principalId = assignment.principalId.toString();

            if (principalType.equals("User") && principalId.equals(userId)) {
                addRoleIfNotNull(roles, assignment, appId);
            } else if (principalType.equals("Group")) {
                List<User> groupMembers = azureGroupMembersCacheRepository.findAllByPrincipalId(principalId);
                groupMembers.forEach(user -> {
                    if (user.id != null && user.id.equals(userId)) {
                        addRoleIfNotNull(roles, assignment, appId);
                    }
                });
            }
        });

        if (roles.isEmpty()) {
            log.debug("User with email {} has no roles assigned in app {}", email, appId);
        } else {
            log.info("User with email {} has the following roles in app {}: {}", email, appId, roles);
        }

        return roles;
    }

    private void addRoleIfNotNull(List<String> roles, AppRoleAssignment assignment, String appId) {
        String roleName = getAppRoleValue(assignment.appRoleId, appId);
        if (roleName != null) {
            roles.add(roleName);
        } else {
            log.debug("Role name for appRoleId {} not found", assignment.appRoleId);
        }
    }

    private String getAppRoleValue(UUID appRoleId, String appId) {
        Map<String, AppRole> appRoles = azureAppRoleCacheRepository.findAll();
//        if (appRoles == null) {
//            log.debug("App roles are null for appId: {}", appId);
//            return null;
//        }

        for (Map.Entry<String, AppRole> entry : appRoles.entrySet()) {
            AppRole appRole = entry.getValue();
            if (appRole.id != null && appRole.id.equals(appRoleId)) {
                return appRole.value;
            }
        }

        log.debug("Role with appRoleId: {} not found in appId: {}", appRoleId, appId);
        return null;
    }

}
