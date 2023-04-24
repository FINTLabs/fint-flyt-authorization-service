package no.fintlabs;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class ClientAuthorization {
    private final boolean authorized;
    private final String clientId;
    private final String sourceApplicationId;
}
