package no.fintlabs.flyt.authorization.user.azure.configuration;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.flyt.authorization.user.azure.models.UserDisplayText;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;


@Configuration
public class UserDisplayTextCacheConfiguration {

    @Bean
    public FintCache<UUID, UserDisplayText> userDisplayTextCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache("userDisplayText", UUID.class, UserDisplayText.class);
    }

}