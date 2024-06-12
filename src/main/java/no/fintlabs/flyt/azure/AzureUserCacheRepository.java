package no.fintlabs.flyt.azure;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AzureUserCacheRepository {
    private final Map<String, AzureUserCache> userCache = new ConcurrentHashMap<>();

    public void save(AzureUserCache user) {
        userCache.put(user.getObjectIdentifier(), user);
    }

    public AzureUserCache findByObjectIdentifier(String objectIdentifier) {
        return userCache.get(objectIdentifier);
    }

    public void deleteByObjectIdentifier(String objectIdentifier) {
        userCache.remove(objectIdentifier);
    }

    public boolean existsByObjectIdentifier(String objectIdentifier) {
        return userCache.containsKey(objectIdentifier);
    }
}
