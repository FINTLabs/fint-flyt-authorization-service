package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.models.DirectoryObjectGetByIdsParameterSet;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import no.fintlabs.flyt.azure.models.GraphUserInfo;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GraphUserService {

    private final GraphServiceClient<Request> graphServiceClient;

    public GraphUserService(GraphServiceClient<Request> graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    // TODO eivindmorch 27/06/2024 : Do pull and cache of display info here
    public List<GraphUserInfo> getUserInfo(Collection<UUID> userIds) {
        return graphServiceClient.users()
                .getByIds(DirectoryObjectGetByIdsParameterSet
                        .newBuilder()
                        .withIds(userIds.stream().map(UUID::toString).toList())
                        .build()
                ).buildRequest()
                .select("id,mail,displayName")
                .post()
//                .setRawObject(new DefaultSerializer(), UserInfo.class)
                .getCurrentPage() // TODO eivindmorch 27/06/2024 : handle multi page
                .stream()
                .filter(user -> user instanceof User)
                .map(user -> (User) user)
                .filter(user -> Objects.nonNull(user.id))
                .map(user -> GraphUserInfo
                        .builder()
                        .id(UUID.fromString(user.id))
                        .displayName(user.displayName)
                        .mail(user.mail)
                        .build()
                )
                .toList();
    }

}
