package no.fintlabs.flyt.azure.services;

import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GraphGroupService {

    private final GraphServiceClient<Request> graphServiceClient;

    public GraphGroupService(GraphServiceClient<Request> graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    public Set<UUID> getGroupUserMemberIds(Collection<UUID> groupIds) {
        return groupIds.stream()
                .map(this::getGroupUserMemberIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    // TODO eivindmorch 27/06/2024 : Only include of principal type User
    public Set<UUID> getGroupUserMemberIds(UUID groupId) {
        return graphServiceClient
                .groups(groupId.toString())
                .members()
                .buildRequest()
                .select("id")
                .get().getCurrentPage()  // TODO eivindmorch 27/06/2024 : Get all pages
                .stream()
                .map(directoryObject -> directoryObject.id)
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }


    // TODO eivindmorch 27/06/2024 : Move to util
//    public List<T> getContentFromCurrentAndNextPages(DirectoryObjectGetByIdsCollectionRequestBuilder baseActionCollectionRequest) {
//        DirectoryObjectGetByIdsCollectionPage postResult = baseActionCollectionRequest.buildRequest().post();
//        if (postResult == null) {
//            throw new IllegalStateException("Post result is null");
//        }
//        ArrayList<DirectoryObject> currentContent = new ArrayList<>(postResult.getCurrentPage());
//        if (postResult.getNextPage() != null) {
//            currentContent.addAll(getContentFromCurrentAndNextPages(postResult.getNextPage()));
//        }
//
//    }

}
