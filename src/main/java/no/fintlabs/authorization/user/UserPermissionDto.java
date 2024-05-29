package no.fintlabs.authorization.user;

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
public class UserPermissionDto {
    @NotNull
    private String objectIdentifier;
    private String email;
    @NotNull
    private List<Integer> sourceApplicationIds;
}
