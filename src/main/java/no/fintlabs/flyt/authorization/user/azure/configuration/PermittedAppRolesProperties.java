package no.fintlabs.flyt.authorization.user.azure.configuration;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class PermittedAppRolesProperties {
    @NotEmpty
    private String flytUser;
}
