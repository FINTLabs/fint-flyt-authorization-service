package no.novari.flyt.authorization.client.sourceapplications.model;

import lombok.Getter;

@Getter
public abstract class BaseSourceApplication implements SourceApplication {
    private final String clientId;
    private final long id;
    private final String displayName;
    private final boolean available;

    protected BaseSourceApplication(long id, String displayName, String clientId, boolean available) {
        this.id = id;
        this.displayName = displayName;
        this.clientId = clientId;
        this.available = available;
    }
}
