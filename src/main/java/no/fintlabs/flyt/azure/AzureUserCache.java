package no.fintlabs.flyt.azure;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AzureUserCache {
    private String objectIdentifier;
    private String email;
}
