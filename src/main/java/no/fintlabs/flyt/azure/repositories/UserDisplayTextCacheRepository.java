package no.fintlabs.flyt.azure.repositories;

import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public Optional<UserDisplayText> findByObjectIdentifier(String objectIdentifier) {
        return userDisplayTextCache.getOptional(objectIdentifier);
    }

    public FintCache<String, UserDisplayText> findAll() {
        return userDisplayTextCache;
    }

    public void deleteByObjectIdentifier(String objectIdentifier) {
        userDisplayTextCache.remove(objectIdentifier);
    }
}
