package no.fintlabs.flyt.authorization.user.azure.services;

import com.microsoft.graph.models.DirectoryObjectGetByIdsParameterSet;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.azure.models.GraphUserInfo;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class GraphUserService {

    private final GraphServiceClient<Request> graphServiceClient;

    private final GraphPageWalkerService graphPageWalkerService;

    public GraphUserService(
            GraphServiceClient<Request> graphServiceClient,
            GraphPageWalkerService graphPageWalkerService
    ) {
        this.graphServiceClient = graphServiceClient;
        this.graphPageWalkerService = graphPageWalkerService;
    }

    public List<GraphUserInfo> getUserInfo(Collection<UUID> userIds) {
        log.info("Retrieving user information for {} user ids", userIds.size());
        List<GraphUserInfo> graphUserInfos =
                graphPageWalkerService.getContentFromCurrentAndNextPages(
                        graphServiceClient.users()
                                .getByIds(DirectoryObjectGetByIdsParameterSet
                                        .newBuilder()
                                        .withIds(userIds.stream().map(UUID::toString).toList())
                                        .build()
                                ).buildRequest()
                                .select("id,mail,displayName"),
                        pageContent -> pageContent.stream()
                                .filter(user -> user instanceof User)
                                .map(user -> (User) user)
                                .filter(user -> Objects.nonNull(user.id))
                                .map(user -> GraphUserInfo
                                        .builder()
                                        .id(UUID.fromString(user.id))
                                        .displayName(user.displayName)
                                        .mail(user.mail)
                                        .build()
                                ).toList()
                );
        log.info("Successfully retrieved user information for {} users", graphUserInfos.size());
        return graphUserInfos;
    }

}
