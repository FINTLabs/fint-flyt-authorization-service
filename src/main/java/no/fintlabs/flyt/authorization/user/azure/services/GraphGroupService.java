package no.fintlabs.flyt.authorization.user.azure.services;

import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
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
        log.info("Retrieving group member ids for groups with ids: {}", groupIds);
        Set<UUID> groupUserMemberIds = groupIds.stream()
                .map(this::getGroupUserMemberIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        log.info("Successfully retrieved {} group members' ids", groupUserMemberIds.size());
        return groupUserMemberIds;
    }

    // TODO eivindmorch 27/06/2024 : Only include of principal type User
    public Set<UUID> getGroupUserMemberIds(UUID groupId) {
        log.info("Retrieving group member ids for group with id: {}", groupId);
        Set<UUID> groupMemberIds = graphPageWalkerService.getContentFromCurrentAndNextPages(
                        graphServiceClient
                                .groups(groupId.toString())
                                .members()
                                .buildRequest()
                                .select("id"),
                        pageContent -> pageContent.stream()
                                .map(directoryObject -> directoryObject.id)
                                .filter(Objects::nonNull)
                                .toList()
                )
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        log.info("Successfully retrieved {} group member ids", groupMemberIds.size());
        return groupMemberIds;
    }

}
