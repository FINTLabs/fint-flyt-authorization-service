package no.fintlabs.flyt.authorization.user.azure.services;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.authorization.user.azure.configuration.AzureAdGatewayConfiguration;
import no.fintlabs.flyt.authorization.user.azure.configuration.AzureCredentialsConfiguration;
import no.fintlabs.flyt.authorization.user.azure.models.GraphUserInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GraphService {

    private final AzureCredentialsConfiguration azureCredentialsConfiguration;

    private final AzureAdGatewayConfiguration azureAdGatewayConfiguration;

    private final GraphServicePrincipalService graphServicePrincipalService;

    private final GraphAppRoleService graphAppRoleService;

    private final GraphGroupService graphGroupService;

    private final GraphUserService graphUserService;

    public GraphService(
            AzureCredentialsConfiguration azureCredentialsConfiguration,
            AzureAdGatewayConfiguration azureAdGatewayConfiguration,
            GraphServicePrincipalService graphServicePrincipalService,
            GraphAppRoleService graphAppRoleService,
            GraphGroupService graphGroupService,
            GraphUserService graphUserService
    ) {
        this.azureCredentialsConfiguration = azureCredentialsConfiguration;
        this.azureAdGatewayConfiguration = azureAdGatewayConfiguration;
        this.graphServicePrincipalService = graphServicePrincipalService;
        this.graphAppRoleService = graphAppRoleService;
        this.graphGroupService = graphGroupService;
        this.graphUserService = graphUserService;
    }

    public List<GraphUserInfo> getPermittedUsersInfo() {
        log.info("Retrieving permitted users info");

        // TODO eivindmorch 27/06/2024 : Hvorfor bruker vi app id og ikke service principal id direkte?
//        UUID servicePrincipalId = UUID.fromString(""); // TODO eivindmorch 27/06/2024 :  Get from config
        UUID servicePrincipalId = graphServicePrincipalService.getServicePrincipalId(
                UUID.fromString(azureCredentialsConfiguration.getAppId())
        );

        Set<UUID> permittedAppRoleIds = graphAppRoleService.getAppRoleIdsFromAppRoleValues(
                servicePrincipalId,
                Set.of(azureAdGatewayConfiguration.getPermittedAppRolesProperties().getFlytUser())
        );

        Set<UUID> permittedUserIds = getPermittedUserIds(
                servicePrincipalId,
                permittedAppRoleIds
        );

        List<GraphUserInfo> userInfo = graphUserService.getUserInfo(permittedUserIds);
        log.info("Successfully retrieved {} permitted users", permittedUserIds.size());
        return userInfo;
    }

    private Set<UUID> getPermittedUserIds(UUID servicePrincipalId, Set<UUID> permittedAppRoleIds) {
        List<GraphAppRoleService.AppRoleIdAndPrincipalSelection> appRoleAssignments =
                graphAppRoleService.getAppRoleAssignmentsWithAppRoleIds(servicePrincipalId, permittedAppRoleIds);

        Map<String, Set<UUID>> idsOfPrincipalsWithPermittedAppRoleAssignmentsPerPrincipalType =
                appRoleAssignments.stream()
                        .collect(Collectors.groupingBy(
                                GraphAppRoleService.AppRoleIdAndPrincipalSelection::getPrincipalType,
                                Collectors.mapping(
                                        GraphAppRoleService.AppRoleIdAndPrincipalSelection::getPrincipalId,
                                        Collectors.toSet()
                                )
                        ));

        return Stream.concat(
                graphGroupService.getGroupUserMemberIds(
                        idsOfPrincipalsWithPermittedAppRoleAssignmentsPerPrincipalType.get("Group")
                ).stream(),
                idsOfPrincipalsWithPermittedAppRoleAssignmentsPerPrincipalType.get("User").stream()
        ).collect(Collectors.toSet());
    }

}
