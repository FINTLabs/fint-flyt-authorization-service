package no.fintlabs.flyt.authorization.user.azure.services;

import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class GraphGroupService {

    private final GraphServiceClient<Request> graphServiceClient;
    private final GraphPageWalkerService graphPageWalkerService;

    public GraphGroupService(
            GraphServiceClient<Request> graphServiceClient,
            GraphPageWalkerService graphPageWalkerService
    ) {
        this.graphServiceClient = graphServiceClient;
        this.graphPageWalkerService = graphPageWalkerService;
    }

    public Set<UUID> getGroupUserMemberIds(Collection<UUID> groupIds) {
        log.info("Retrieving member IDs for groups with the following IDs: {}", groupIds);
        Set<UUID> groupUserMemberIds = groupIds.stream()
                .map(this::getGroupUserMemberIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        log.info("Successfully retrieved member IDs for {} groups, totaling {} member IDs", groupIds.size(), groupUserMemberIds.size());
        return groupUserMemberIds;
    }

    public Set<UUID> getGroupUserMemberIds(UUID groupId) {
        log.info("Retrieving member IDs for group with ID: {}", groupId);
        Set<UUID> groupMemberIds = graphPageWalkerService.getContentFromCurrentAndNextPages(
                        graphServiceClient
                                .groups(groupId.toString())
                                .membersAsUser()
                                .buildRequest()
                                .select("id"),
                        pageContent -> pageContent.stream()
                                .map(user -> user.id)
                                .filter(Objects::nonNull)
                                .toList()
                )
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        log.info("Successfully retrieved {} member IDs for group with ID: {}", groupMemberIds.size(), groupId);
        return groupMemberIds;
    }

}
