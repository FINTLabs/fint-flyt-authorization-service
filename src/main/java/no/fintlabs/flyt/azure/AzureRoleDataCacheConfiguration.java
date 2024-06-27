package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.AppRole;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import no.fintlabs.flyt.azure.models.wrappers.AppRoleAssignmentWrapper;
import no.fintlabs.flyt.azure.models.wrappers.GroupMembersWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureRoleDataCacheConfiguration {

    @Bean
    public FintCache<String, UserDisplayText> userDisplayTextCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache("userDisplayText", String.class, UserDisplayText.class);
    }

    @Bean
    public FintCache<String, AppRole> appRoleCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache("appRoleCache", String.class, AppRole.class);
    }

    @Bean
    public FintCache<String, AppRoleAssignmentWrapper> appRoleAssignmentsCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache("appRoleAssignmentsCache", String.class, AppRoleAssignmentWrapper.class);
    }

    @Bean
    public FintCache<String, GroupMembersWrapper> groupMembersCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache("groupMembersCache", String.class, GroupMembersWrapper.class);
    }

}