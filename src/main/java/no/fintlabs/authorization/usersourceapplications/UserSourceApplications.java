package no.fintlabs.authorization.usersourceapplications;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UserSourceApplications {
    private List<Integer> sourceApplicationIds;
}
