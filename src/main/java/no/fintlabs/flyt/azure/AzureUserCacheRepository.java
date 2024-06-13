package no.fintlabs.flyt.azure;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AzureUserCacheRepository {
    private final Map<String, AzureUserCache> userCache = new ConcurrentHashMap<>();

    public void save(AzureUserCache azureUserCache) {
        userCache.put(azureUserCache.getObjectIdentifier(), azureUserCache);
    }

    public void saveAll(List<AzureUserCache> azureUserCaches) {
        azureUserCaches.forEach(this::save);
    }

    public AzureUserCache findByObjectIdentifier(String objectIdentifier) {
        return userCache.get(objectIdentifier);
    }

    public Map<String, AzureUserCache> findAll() {
        return userCache;
    }

    public void deleteByObjectIdentifier(String objectIdentifier) {
        userCache.remove(objectIdentifier);
    }

    public boolean existsByObjectIdentifier(String objectIdentifier) {
        return userCache.containsKey(objectIdentifier);
    }
}
