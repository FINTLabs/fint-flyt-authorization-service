package no.fintlabs.flyt.azure.models.wrappers;

import com.microsoft.graph.models.AppRoleAssignment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class AppRoleAssignmentWrapper {
    private List<AppRoleAssignment> appRoleAssignments;
}
