package no.fintlabs.flyt.azure.repositories;

import no.fintlabs.flyt.azure.models.UserDisplayText;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AzureUserCacheRepository {
    private final Map<String, UserDisplayText> azureUserCacheMap = new ConcurrentHashMap<>();

//    public void save(UserDisplayText userDisplayText) {
//        azureUserCacheMap.put(userDisplayText.getObjectIdentifier(), userDisplayText);
//    }

//    public void saveAll(List<UserDisplayText> userDisplayTextCaches) {
//        userDisplayTextCaches.forEach(this::save);
//    }

    public UserDisplayText findByObjectIdentifier(String objectIdentifier) {
        return azureUserCacheMap.get(objectIdentifier);
    }

    public Map<String, UserDisplayText> findAll() {
        return azureUserCacheMap;
    }

    public void deleteByObjectIdentifier(String objectIdentifier) {
        azureUserCacheMap.remove(objectIdentifier);
    }

}
