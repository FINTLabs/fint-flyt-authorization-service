package no.fintlabs.flyt.azure;

import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import no.fintlabs.flyt.authorization.user.UserPermission;
import no.fintlabs.flyt.authorization.user.UserPermissionRepository;
import no.fintlabs.flyt.authorization.user.UserPermissionService;
import okhttp3.Request;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Log4j2
@RequiredArgsConstructor
public class AzureADUserSyncService {
    protected final Config config;

    protected final ConfigUser configUser;
    protected final GraphServiceClient<Request> graphService;
    protected final AzureRoleService azureRoleService;

    protected final UserPermissionService userPermissionService;

    protected final UserPermissionRepository userPermissionRepository;
    protected final AzureUserCacheRepository azureUserCacheRepository;

    private void processUsers(UserCollectionPage userCollectionPage) {
        int usersProcessed = 0;
        UserCollectionPage currentPage = userCollectionPage;

        List<UserPermission> userPermissionList = new ArrayList<>();

        do {
            for (User user : currentPage.getCurrentPage()) {
                usersProcessed++;

                List<String> userRoles = azureRoleService.getUserRoles(user.id, user.mail, config.getAppid());

                if (userRoles.contains("https://role-catalog.vigoiks.no/vigo/flyt/user") || userRoles.contains("https://role-catalog.vigoiks.no/vigo/flyt/developer")) {
                    UserPermission userPermission = UserPermission
                            .builder()
                            .objectIdentifier(user.id)
                            .build();
                    userPermissionList.add(userPermission);

                    AzureUserCache azureUserCache = AzureUserCache
                            .builder()
                            .objectIdentifier(user.id)
                            .email(Objects.requireNonNull(user.mail).toLowerCase())
                            .build();
                    azureUserCacheRepository.save(azureUserCache);
                }

            }
            if (currentPage.getNextPage() != null) {
                currentPage = currentPage.getNextPage().buildRequest().get();
            } else {
                currentPage = null;
            }
        } while (currentPage != null);

        userPermissionService.saveUserPermissions(userPermissionList);

        log.info("{} User objects processed in Azure AD", usersProcessed);
    }

//    public List<AzureGroup> getGroup(UUID principalId) {
//        return this.pageThroughGetGroups(
//                graphService.groups()
//                        .buildRequest()
//                        .select("id,members"))
//                        //.filter(String.format("startsWith(displayName,'%s')",configGroup.getPrefix()))
//                        .expand("members($select=id)")
//                        .get()
//        );
//    }

//    private List<AzureGroup> pageThroughGetGroups(GroupCollectionPage inPage) {
//        int groups = 0;
//        GroupCollectionPage page = inPage;
//        List<AzureGroup> retGroupList = new ArrayList<AzureGroup>();
//        do {
//            for (Group group : page.getCurrentPage()) {
//
//                AzureGroup newGroup;
//                try {
//                    newGroup = new AzureGroup(group);
//                } catch (NumberFormatException e) {
//                    log.warn("Problems converting resourceID to LONG! {}. Skipping creation of group", e);
//                    continue;
//                }
//                retGroupList.add(newGroup);
//            }
//            if (page.getNextPage() == null) {
//                break;
//            } else {
//                log.debug("Processing group page");
//                page = page.getNextPage().buildRequest().get();
//            }
//        } while (page != null);
//        log.debug("{} Group objects detected in Microsoft Entra", groups);
//        return retGroupList;
//    }

    @Scheduled(
            initialDelayString = "${fint.flyt.azure-ad-gateway.user-scheduler.pull.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.azure-ad-gateway.user-scheduler.pull.fixed-delay-ms}"
    )
    private void pullUsers() {
        log.info("*** <<< Starting to pull users from Azure AD >>> ***");
        long startTime = System.currentTimeMillis();

        try {
            UserCollectionPage usersPage = graphService.users()
                    .buildRequest()
                    .select(String.join(",", configUser.allAttributes()))
                    .filter("usertype eq 'member'")
                    .get();

            processUsers(usersPage);
        } catch (Exception e) {
            log.error("Error fetching users : {}", e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTimeInSeconds = (endTime - startTime) / 1000;
        long minutes = elapsedTimeInSeconds / 60;
        long seconds = elapsedTimeInSeconds % 60;

        log.info("*** <<< Finished pulling users from Azure AD in {} minutes and {} seconds >>> *** ", minutes, seconds);
    }


}
