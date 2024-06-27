package no.fintlabs.flyt.azure.repositories;

import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDisplayTextCacheRepository {
    private final FintCache<String, UserDisplayText> userDisplayTextCache;

    public UserDisplayTextCacheRepository(FintCache<String, UserDisplayText> userDisplayTextCache) {
        this.userDisplayTextCache = userDisplayTextCache;
    }

    public void save(UserDisplayText userDisplayText) {
        userDisplayTextCache.put(userDisplayText.getObjectIdentifier(), userDisplayText);
    }

    public void saveAll(List<UserDisplayText> userDisplayTextCaches) {
        userDisplayTextCaches.forEach(this::save);
    }

    public UserDisplayText findByObjectIdentifier(String objectIdentifier) {
        return userDisplayTextCache.get(objectIdentifier);
    }

    public FintCache<String, UserDisplayText> findAll() {
        return userDisplayTextCache;
    }

    public void deleteByObjectIdentifier(String objectIdentifier) {
        userDisplayTextCache.remove(objectIdentifier);
    }

}
