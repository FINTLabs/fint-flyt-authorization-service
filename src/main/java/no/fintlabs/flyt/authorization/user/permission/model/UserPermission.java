package no.fintlabs.flyt.authorization.user.permission.model;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class UserPermission {
    @NotNull
    private UUID objectIdentifier;
    @NotNull
    private List<Integer> sourceApplicationIds;
}
