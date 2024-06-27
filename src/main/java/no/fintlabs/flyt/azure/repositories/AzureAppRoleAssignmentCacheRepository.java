package no.fintlabs.flyt.azure.repositories;

import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.wrappers.AppRoleAssignmentWrapper;
import org.springframework.stereotype.Repository;

@Repository
public class AzureAppRoleAssignmentCacheRepository {

    private final FintCache<String, AppRoleAssignmentWrapper> appRoleAssignmentsCache;

    public AzureAppRoleAssignmentCacheRepository(FintCache<String, AppRoleAssignmentWrapper> appRoleAssignmentsCache) {
        this.appRoleAssignmentsCache = appRoleAssignmentsCache;
    }

    public void saveAll(String appId, AppRoleAssignmentWrapper appRoleAssignments) {
        appRoleAssignmentsCache.put(appId, appRoleAssignments);
    }

    public AppRoleAssignmentWrapper findAllByAppId(String appId) {
        return appRoleAssignmentsCache.get(appId);
    }

}
