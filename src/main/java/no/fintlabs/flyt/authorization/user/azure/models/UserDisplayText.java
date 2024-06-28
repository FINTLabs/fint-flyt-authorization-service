package no.fintlabs.flyt.authorization.user.azure.models;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserDisplayText {
    private UUID objectIdentifier;
    private String email;
    private String name;
}
