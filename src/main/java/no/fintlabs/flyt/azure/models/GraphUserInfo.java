package no.fintlabs.flyt.azure.models;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class GraphUserInfo {
    private final UUID id;
    private final String displayName;
    private final String mail;
}
