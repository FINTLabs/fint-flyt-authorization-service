package no.novari.flyt.authorization.user.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RestrictedPageAuthorization {
    private boolean userPermissionPage;
}
