package no.fintlabs.flyt.authorization.user.permission;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RestrictedPageAuthorization {
    private boolean userPermissionPage;
}
