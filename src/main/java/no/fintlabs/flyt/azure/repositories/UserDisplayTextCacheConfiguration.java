package no.fintlabs.flyt.azure.repositories;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDisplayTextCacheConfiguration {

    @Bean
    public FintCache<String, UserDisplayText> userDisplayTextCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache("userDisplayText", String.class, UserDisplayText.class);
    }
}
