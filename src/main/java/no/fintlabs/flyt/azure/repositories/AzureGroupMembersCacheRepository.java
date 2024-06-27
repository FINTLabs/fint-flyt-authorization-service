package no.fintlabs.flyt.azure.repositories;

import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.wrappers.GroupMembersWrapper;
import org.springframework.stereotype.Repository;

@Repository
public class AzureGroupMembersCacheRepository {

    private final FintCache<String, GroupMembersWrapper> groupMembersCache;

    public AzureGroupMembersCacheRepository(FintCache<String, GroupMembersWrapper> groupMembersCache) {
        this.groupMembersCache = groupMembersCache;
    }

    public void saveAll(String principalId, GroupMembersWrapper groupMembers) {
        groupMembersCache.put(principalId, groupMembers);
    }

    public GroupMembersWrapper findAllByPrincipalId(String principalId) {
        return groupMembersCache.get(principalId);
    }

}
