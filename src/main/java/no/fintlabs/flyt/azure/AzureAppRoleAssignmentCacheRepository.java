package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.AppRoleAssignment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AzureAppRoleAssignmentCacheRepository {

    private final List<AppRoleAssignment> appRoleAssignmentsCache = new ArrayList<>();

    public void saveAll(List<AppRoleAssignment> appRoleAssignments) {
        appRoleAssignmentsCache.addAll(appRoleAssignments);
    }

    public List<AppRoleAssignment> findAll() {
        return appRoleAssignmentsCache;
    }

}
