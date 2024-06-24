package no.fintlabs.flyt.azure.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AzureUserCache {
    private String objectIdentifier;
    private String email;
    private String name;
}
