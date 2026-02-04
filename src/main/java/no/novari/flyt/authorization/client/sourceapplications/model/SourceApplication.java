package no.novari.flyt.authorization.client.sourceapplications.model;

public interface SourceApplication {
    long getId();
    String getClientId();
    String getDisplayName();
    boolean isAvailable();
}
