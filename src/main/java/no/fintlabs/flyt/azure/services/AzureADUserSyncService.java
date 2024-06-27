package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.userpermission.UserPermissionEntity;
import no.fintlabs.flyt.authorization.userpermission.UserPermissionService;
import no.fintlabs.flyt.azure.Config;
import no.fintlabs.flyt.azure.StringUtils;
import no.fintlabs.flyt.azure.models.UserDisplayText;
import no.fintlabs.flyt.azure.models.ConfigUser;
import no.fintlabs.flyt.azure.models.PermittedAppRoles;
import okhttp3.Request;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class AzureADUserSyncService {
    protected final Config config;
    protected final ConfigUser configUser;
    protected final PermittedAppRoles permittedAppRoles;
    protected final GraphServiceClient<Request> graphService;
    protected final UserPermissionService userPermissionService;
    protected final AzureAppRoleCacheService azureAppRoleCacheService;
    protected final AzureUserCacheService azureUserCacheService;

    @Scheduled(
            initialDelayString = "${fint.flyt.azure-ad-gateway.user-scheduler.pull.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.azure-ad-gateway.user-scheduler.pull.fixed-delay-ms}"
    )
    private void pullUsersAndRoles() {

        validateConfig();

        log.info("*** <<< Starting to pull users from Azure AD >>> ***");
        long startTime = System.currentTimeMillis();

        azureAppRoleCacheService.storeAzureAppRoleDataInCache(config.getAppId());

        try {
            UserCollectionPage usersPage = graphService.users()
                    .buildRequest()
                    .select(String.join(",", configUser.allAttributes()))
                    .filter("usertype eq 'member'")
                    .get();

            processUsers(usersPage);

            long endTime = System.currentTimeMillis();
            long elapsedTimeInSeconds = (endTime - startTime) / 1000;
            long minutes = elapsedTimeInSeconds / 60;
            long seconds = elapsedTimeInSeconds % 60;

            log.info("*** <<< Finished pulling users from Azure AD in {} minutes and {} seconds >>> *** ", minutes, seconds);
        } catch (Exception e) {
            log.error("Error fetching users : {}", e.getMessage(), e);
        }
    }

    private void validateConfig() {
        if (isNullOrEmpty(config.getClientId()) ||
                isNullOrEmpty(config.getClientSecret()) ||
                isNullOrEmpty(config.getTenantId()) ||
                isNullOrEmpty(config.getAppId())) {
            throw new IllegalArgumentException("Azure AD configuration is not properly set.");
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value);
    }

    private void processUsers(UserCollectionPage userCollectionPage) {
        int usersProcessed = 0;
        UserCollectionPage currentPage = userCollectionPage;

        List<UserPermissionEntity> userPermissionEntityList = new ArrayList<>();
        List<UserDisplayText> userDisplayTextCaches = new ArrayList<>();
        try {
            do {
                for (User user : currentPage.getCurrentPage()) {

                    if (user.mail == null) {
                        continue;
                    }

                    usersProcessed++;

                    List<String> userRoles = azureAppRoleCacheService.getUserRoles(user.id, user.mail, config.getAppId());

                    if (isPermittedRole(userRoles)) {
                        UserPermissionEntity userPermissionEntity = UserPermissionEntity
                                .builder()
                                .objectIdentifier(user.id)
                                .build();
                        userPermissionEntityList.add(userPermissionEntity);

                        UserDisplayText userDisplayText = UserDisplayText
                                .builder()
                                .objectIdentifier(user.id)
                                .email(Objects.requireNonNull(user.mail).toLowerCase())
                                .name(StringUtils.capitalizeFirstLetterOfEachWord(user.givenName) + " " + user.surname)
                                .build();
                        userDisplayTextCaches.add(userDisplayText);
                    }

                }
                if (currentPage.getNextPage() != null) {
                    currentPage = currentPage.getNextPage().buildRequest().get();
                } else {
                    currentPage = null;
                }
            } while (currentPage != null);

            userPermissionService.refreshUserPermissions(userPermissionEntityList);
            userPermissionEntityList.forEach(userPermission -> log.info("Saving user permission {} in db", userPermission.getObjectIdentifier()));

            azureUserCacheService.refreshAzureUserCaches(userDisplayTextCaches);
            userDisplayTextCaches.forEach(azureUserCache -> log.debug("Saving azure user {} in cache", azureUserCache.getEmail()));

            log.info("{} User objects processed in Azure AD", usersProcessed);
        } catch (Exception e) {
            log.error("An error occurred while processing user permissions: {}", e.getMessage());
        }
    }

    private boolean isPermittedRole(List<String> userRoles) {
        for (String role : userRoles) {
            if (permittedAppRoles.getPermittedAppRoles().containsValue(role)) {
                return true;
            }
        }
        return false;
    }

}
