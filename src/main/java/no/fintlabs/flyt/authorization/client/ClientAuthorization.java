package no.fintlabs.flyt.authorization.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientAuthorization {
    private final boolean authorized;
    private final String clientId;
    private final Long sourceApplicationId; // TODO eivindmorch 28/06/2024 : Endret til long. Fikse andre steder?
}
