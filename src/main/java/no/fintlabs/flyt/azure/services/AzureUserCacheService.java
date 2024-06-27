package no.fintlabs.flyt.azure.services;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import no.fintlabs.flyt.azure.repositories.UserDisplayTextCacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AzureUserCacheService {

    private final UserDisplayTextCacheRepository userDisplayTextCacheRepository;

    public AzureUserCacheService(UserDisplayTextCacheRepository userDisplayTextCacheRepository) {
        this.userDisplayTextCacheRepository = userDisplayTextCacheRepository;
    }

    public void refreshAzureUserCaches(List<UserDisplayText> userDisplayTextCaches) {
        deleteAzureUserCacheNotInList(userDisplayTextCaches);
        userDisplayTextCacheRepository.saveAll(userDisplayTextCaches);
    }

    private void deleteAzureUserCacheNotInList(List<UserDisplayText> userDisplayTextCaches) {
        FintCache<String, UserDisplayText> allCurrentUserDisplayTextCaches = userDisplayTextCacheRepository.findAll();

        List<String> inputUserDisplayTextCachesIdentifiers = userDisplayTextCaches.stream()
                .map(UserDisplayText::getObjectIdentifier)
                .toList();

        List<UserDisplayText> allCurrentEntries = allCurrentUserDisplayTextCaches.getAll();

        List<String> allCurrentKeys = allCurrentEntries.stream()
                .map(UserDisplayText::getObjectIdentifier)
                .toList();

        List<String> keysToDelete = allCurrentKeys.stream()
                .filter(key -> !inputUserDisplayTextCachesIdentifiers.contains(key))
                .toList();

        keysToDelete.forEach(userDisplayTextCacheRepository::deleteByObjectIdentifier);

        if (!keysToDelete.isEmpty()) {
            log.info("Deleted {} user permissions", keysToDelete.size());
        }
    }
}
