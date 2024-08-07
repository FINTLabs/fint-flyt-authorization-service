package no.fintlabs.flyt.authorization.user.kafka;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserPermission {
    private UUID objectIdentifier;
    private List<Long> sourceApplicationIds;
}
