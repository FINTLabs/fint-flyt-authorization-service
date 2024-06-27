package no.fintlabs.flyt.azure.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDisplayText {
    private String objectIdentifier;
    private String email;
    private String name;
}
