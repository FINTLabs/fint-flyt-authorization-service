package no.fintlabs.authorization.user;

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
    private List<Integer> sourceApplicationIds;
}
