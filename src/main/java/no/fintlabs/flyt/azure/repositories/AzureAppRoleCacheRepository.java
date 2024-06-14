package no.fintlabs.flyt.azure.repositories;

import com.microsoft.graph.models.AppRole;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.azure.models.PermittedAppRoles;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class AzureAppRoleCacheRepository {

    private final Map<String, AppRole> appRoleCache = new HashMap<>();
    protected final PermittedAppRoles permittedAppRoles;

    public AzureAppRoleCacheRepository(PermittedAppRoles permittedAppRoles) {
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

    public Map<String, AppRole> findAll() {
        return appRoleCache;
    }
}
