package no.fintlabs;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientAuthorization {
    private final boolean authorized;
    private final String clientId;
    private final String sourceApplicationId;
}
