package no.fintlabs.flyt.azure.services;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.azure.repositories.AzureUserCacheRepository;
import no.fintlabs.flyt.azure.models.UserDisplayText;
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

    public void refreshAzureUserCaches(List<UserDisplayText> userDisplayTextCaches) {
        deleteAzureUserCacheNotInList(userDisplayTextCaches);
        azureUserCacheRepository.saveAll(userDisplayTextCaches);
    }

    private void deleteAzureUserCacheNotInList(List<UserDisplayText> userDisplayTextCaches) {
        Map<String, UserDisplayText> allCurrentAzureUserCaches = azureUserCacheRepository.findAll();

        List<String> inputAzureUserCachesIdentifiers = userDisplayTextCaches.stream()
                .map(UserDisplayText::getObjectIdentifier)
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
