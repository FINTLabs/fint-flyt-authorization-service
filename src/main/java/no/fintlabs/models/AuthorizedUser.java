package no.fintlabs.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthorizedUser {
    private boolean admin;
}
