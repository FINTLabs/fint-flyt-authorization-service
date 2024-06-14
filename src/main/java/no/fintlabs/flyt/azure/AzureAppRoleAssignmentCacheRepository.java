package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.AppRoleAssignment;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AzureAppRoleAssignmentCacheRepository {

    private final Map<String, List<AppRoleAssignment>> appRoleAssignmentsCache = new HashMap<>();

    public void saveAll(String appId, List<AppRoleAssignment> appRoleAssignments) {
        appRoleAssignmentsCache.put(appId, appRoleAssignments);
    }

    public List<AppRoleAssignment> findAllByAppId(String appId) {
        return appRoleAssignmentsCache.get(appId);
    }

}
