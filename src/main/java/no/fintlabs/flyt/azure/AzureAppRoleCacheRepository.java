package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.AppRole;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AzureAppRoleCacheRepository {

    private final List<AppRole> appRoleCache = new ArrayList<>();
    protected final PermittedAppRoles permittedAppRoles;

    public AzureAppRoleCacheRepository(PermittedAppRoles permittedAppRoles) {
        this.permittedAppRoles = permittedAppRoles;
    }

    public void save(AppRole appRole) {
        appRoleCache.add(appRole);
    }

    public void saveAllPermittedRoles(List<AppRole> appRoles) {
        appRoles.forEach(appRole -> {
                    if (permittedAppRoles.getPermittedAppRoles().containsValue(appRole.value)) {
                        save(appRole);
                    }
                }
        );
    }

    public List<AppRole> findAll() {
        return appRoleCache;
    }
}
