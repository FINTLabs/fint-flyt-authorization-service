package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AzureGroupMembersCacheRepository {

    private final Map<String, List<User>> groupMembersCache = new HashMap<>();

    public void saveAll(String principalId, List<User> groupMembers) {
        groupMembersCache.put(principalId, groupMembers);
    }

    public List<User> findAllByPrincipalId(String principalId) {
        return groupMembersCache.get(principalId);
    }

}
