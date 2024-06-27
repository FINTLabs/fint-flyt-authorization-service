package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.models.AppRole;
import com.microsoft.graph.models.AppRoleAssignment;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.wrappers.AppRoleAssignmentWrapper;
import no.fintlabs.flyt.azure.models.wrappers.GroupMembersWrapper;
import no.fintlabs.flyt.azure.repositories.AzureAppRoleAssignmentCacheRepository;
import no.fintlabs.flyt.azure.repositories.AzureAppRoleCacheRepository;
import no.fintlabs.flyt.azure.repositories.AzureGroupMembersCacheRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AzureAppRoleCacheService {

    protected final AzureAppGraphService azureAppGraphService;

    protected final AzureAppRoleCacheRepository azureAppRoleCacheRepository;
    protected final AzureAppRoleAssignmentCacheRepository azureAppRoleAssignmentCacheRepository;
    protected final AzureGroupMembersCacheRepository azureGroupMembersCacheRepository;

    public AzureAppRoleCacheService(
            AzureAppGraphService azureAppGraphService,
            AzureAppRoleCacheRepository azureAppRoleCacheRepository,
            AzureAppRoleAssignmentCacheRepository azureAppRoleAssignmentCacheRepository,
            AzureGroupMembersCacheRepository azureGroupMembersCacheRepository) {
        this.azureAppGraphService = azureAppGraphService;
        this.azureAppRoleCacheRepository = azureAppRoleCacheRepository;
        this.azureAppRoleAssignmentCacheRepository = azureAppRoleAssignmentCacheRepository;
        this.azureGroupMembersCacheRepository = azureGroupMembersCacheRepository;
    }

    public void storeAzureAppRoleDataInCache(String appId) {
        AppRoleAssignmentWrapper appRoleAssignments = azureAppGraphService.getAppRoleAssignments(appId);
        azureAppRoleAssignmentCacheRepository.saveAll(appId, appRoleAssignments);

        appRoleAssignments.getAppRoleAssignments().forEach(appRoleAssignment -> {
            if (appRoleAssignment.principalType == null || appRoleAssignment.principalId == null) {
                log.warn("Assignment principalType or principalId is null for appRoleAssignment {}", appRoleAssignment);
                return;
            }

            String principalType = appRoleAssignment.principalType;
            String principalId = appRoleAssignment.principalId.toString();

            if (principalType.equals("Group")) {
                GroupMembersWrapper groupMembers = azureAppGraphService.getGroupMembers(principalId);
                azureGroupMembersCacheRepository.saveAll(principalId, groupMembers);
            }
        });

        List<AppRole> appRoles = azureAppGraphService.getAppRoles(appId);
        azureAppRoleCacheRepository.saveAllPermittedRoles(appRoles);
    }

    public List<String> getUserRoles(
            String userId,
            String email,
            String appId
    ) {
        List<String> roles = new ArrayList<>();

        AppRoleAssignmentWrapper appRoleAssignments = this.azureAppRoleAssignmentCacheRepository.findAllByAppId(appId);

        appRoleAssignments.getAppRoleAssignments().forEach(assignment -> {
            if (assignment.principalType == null || assignment.principalId == null) {
                log.debug("Assignment principalType or principalId is null for assignment {}", assignment);
                return;
            }

            String principalType = assignment.principalType;
            String principalId = assignment.principalId.toString();

            if (principalType.equals("User") && principalId.equals(userId)) {
                addRoleIfNotNull(roles, assignment, appId);
            } else if (principalType.equals("Group")) {
                GroupMembersWrapper groupMembers = azureGroupMembersCacheRepository.findAllByPrincipalId(principalId);
                groupMembers.getGroupMembers().forEach(user -> {
                    if (user.id != null && user.id.equals(userId)) {
                        addRoleIfNotNull(roles, assignment, appId);
                    }
                });
            }
        });

        if (roles.isEmpty()) {
            log.debug("User with email {} has no roles assigned in app {}", email, appId);
        } else {
            log.debug("User with email {} has the following roles in app {}: {}", email, appId, roles);
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
        FintCache<String, AppRole> appRoles = azureAppRoleCacheRepository.findAll();

        for (AppRole appRole : appRoles.getAll()) {
            if (appRole.id != null && appRole.id.equals(appRoleId)) {
                return appRole.value;
            }
        }

        log.debug("Role with appRoleId: {} not found in appId: {}", appRoleId, appId);
        return null;
    }

}
