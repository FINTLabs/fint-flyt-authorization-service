package no.fintlabs.flyt.azure;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PermittedAppRoles {
    private Map<String, String> permittedAppRoles;
}
