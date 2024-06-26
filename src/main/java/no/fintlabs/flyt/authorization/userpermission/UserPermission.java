package no.fintlabs.flyt.authorization.userpermission;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class UserPermission {
    @NotNull
    private String objectIdentifier;
    @NotNull
    private List<Integer> sourceApplicationIds;
}
