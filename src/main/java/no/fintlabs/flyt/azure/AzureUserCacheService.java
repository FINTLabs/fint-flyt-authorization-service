package no.fintlabs.flyt.azure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AzureUserCacheService {

    private final AzureUserCacheRepository azureUserCacheRepository;

    public AzureUserCacheService(AzureUserCacheRepository azureUserCacheRepository) {
        this.azureUserCacheRepository = azureUserCacheRepository;
    }

    public void refreshAzureUserCaches(List<AzureUserCache> azureUserCaches) {
        deleteAzureUserCacheNotInList(azureUserCaches);
        azureUserCacheRepository.saveAll(azureUserCaches);
    }

    private void deleteAzureUserCacheNotInList(List<AzureUserCache> azureUserCaches) {
        Map<String, AzureUserCache> allCurrentAzureUserCaches = azureUserCacheRepository.findAll();

        List<String> inputAzureUserCachesIdentifiers = azureUserCaches.stream()
                .map(AzureUserCache::getObjectIdentifier)
                .toList();

        List<String> azureUserCachesStringsToDelete = allCurrentAzureUserCaches.keySet().stream()
                .filter(identifier -> !inputAzureUserCachesIdentifiers.contains(identifier))
                .toList();

        azureUserCachesStringsToDelete.forEach(azureUserCacheRepository::deleteByObjectIdentifier);

        if (!azureUserCachesStringsToDelete.isEmpty()) {
            log.info("Deleted {} user permissions", azureUserCachesStringsToDelete.size());
        }
    }
}
