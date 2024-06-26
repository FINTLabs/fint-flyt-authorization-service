package no.fintlabs.flyt.authorization.userpermission;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RestrictedPageAccess {
    private boolean userPermission;
}
