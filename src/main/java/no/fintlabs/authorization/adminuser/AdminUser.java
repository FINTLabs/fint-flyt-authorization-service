package no.fintlabs.authorization.adminuser;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdminUser {
    private boolean admin;
}
