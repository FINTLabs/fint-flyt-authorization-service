package no.fintlabs.models.user;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthorizedUser {
    private boolean admin;
}
