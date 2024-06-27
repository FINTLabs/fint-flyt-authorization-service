package no.fintlabs.flyt.azure.repositories;

import com.microsoft.graph.models.AppRole;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.PermittedAppRoles;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class AzureAppRoleCacheRepository {

    private final FintCache<String, AppRole> appRoleCache;
    protected final PermittedAppRoles permittedAppRoles;

    public AzureAppRoleCacheRepository(
            FintCache<String, AppRole> appRoleCache, PermittedAppRoles permittedAppRoles
    ) {
        this.appRoleCache = appRoleCache;
        this.permittedAppRoles = permittedAppRoles;
    }

    public void save(AppRole appRole) {
        if (appRole.id != null) {
            appRoleCache.put(appRole.id.toString(), appRole);
        } else {
            log.warn("App role {} was not saved in cache because id is null", appRole);
        }
    }

    public void saveAllPermittedRoles(List<AppRole> appRoles) {
        appRoles.forEach(appRole -> {
                    if (permittedAppRoles.getPermittedAppRoles().containsValue(appRole.value)) {
                        save(appRole);
                    }
                }
        );
    }

    public FintCache<String, AppRole> findAll() {
        return appRoleCache;
    }
}
