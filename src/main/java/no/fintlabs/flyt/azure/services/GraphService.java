package no.fintlabs.flyt.azure.services;

import no.fintlabs.flyt.azure.configuration.AzureAdGatewayConfiguration;
import no.fintlabs.flyt.azure.models.GraphUserInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GraphService {

    private final AzureAdGatewayConfiguration azureAdGatewayConfiguration;

    private final GraphAppRoleService graphAppRoleService;

    private final GraphGroupService graphGroupService;

    private final GraphUserService graphUserService;

    public GraphService(
            AzureAdGatewayConfiguration azureAdGatewayConfiguration,
            GraphAppRoleService graphAppRoleService,
            GraphGroupService graphGroupService,
            GraphUserService graphUserService
    ) {
        this.azureAdGatewayConfiguration = azureAdGatewayConfiguration;
        this.graphAppRoleService = graphAppRoleService;
        this.graphGroupService = graphGroupService;
        this.graphUserService = graphUserService;
    }

    public List<GraphUserInfo> getPermittedUsersInfo() {
//        UUID servicePrincipalId = graphServicePrincipalService.getServicePrincipalId(config.getAppId());
        // TODO eivindmorch 27/06/2024 : Hvorfor bruker vi app id og ikke service principal id direkte?
        UUID servicePrincipalId = UUID.fromString(""); // TODO eivindmorch 27/06/2024 :  Get from config

        Set<UUID> permittedAppRoleIds = graphAppRoleService.getAppRoleIdsFromAppRoleValues(
                servicePrincipalId,
                Set.of(azureAdGatewayConfiguration.getPermittedAppRoles().getFlytUser())
        );

        Set<UUID> permittedUserIds = getPermittedUserIds(
                servicePrincipalId,
                permittedAppRoleIds
        );

        return graphUserService.getUserInfo(permittedUserIds);
    }

    private Set<UUID> getPermittedUserIds(UUID servicePrincipalId, Set<UUID> permittedAppRoleIds) {
        List<GraphAppRoleService.AppRoleIdAndPrincipalSelection> appRoleAssignments =
                graphAppRoleService.getAppRoleAssignments(servicePrincipalId);

        Map<String, Set<UUID>> idsOfPrincipalsWithPermittedAppRoleAssignmentsPerPrincipalType = appRoleAssignments.stream()
                .filter(appRoleAssignment -> permittedAppRoleIds.contains(appRoleAssignment.getAppRoleId()))
                .collect(Collectors.groupingBy(
                        GraphAppRoleService.AppRoleIdAndPrincipalSelection::getPrincipalType,
                        Collectors.mapping(
                                GraphAppRoleService.AppRoleIdAndPrincipalSelection::getPrincipalId,
                                Collectors.toSet()
                        )
                ));

        return Stream.concat(
                graphGroupService.getGroupUserMemberIds(idsOfPrincipalsWithPermittedAppRoleAssignmentsPerPrincipalType.get("Group")).stream(),
                idsOfPrincipalsWithPermittedAppRoleAssignmentsPerPrincipalType.get("User").stream()
        ).collect(Collectors.toSet());
    }

}
