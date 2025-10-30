package no.fintlabs.flyt.authorization.user.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@Jacksonized
public class User {
    @NotNull
    private UUID objectIdentifier;

    private String email;

    private String name;

    @NotNull
    @Builder.Default
    private List<Long> sourceApplicationIds = new ArrayList<>();
}
