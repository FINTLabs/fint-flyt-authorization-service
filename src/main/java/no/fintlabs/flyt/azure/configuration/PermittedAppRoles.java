package no.fintlabs.flyt.azure.configuration;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class PermittedAppRoles {
    @NotEmpty
    private String flytUser;
}
