package no.novari.flyt.authorization.client.sourceapplications.model;

import lombok.Getter;

@Getter
public abstract class BaseSourceApplication implements SourceApplication {
    private final String clientId;
    private final long sourceApplicationId;
    private final String displayName;
    private final boolean available;

    protected BaseSourceApplication(long sourceApplicationId, String displayName, String clientId, boolean available) {
        this.sourceApplicationId = sourceApplicationId;
        this.displayName = displayName;
        this.clientId = clientId;
        this.available = available;
    }
}
