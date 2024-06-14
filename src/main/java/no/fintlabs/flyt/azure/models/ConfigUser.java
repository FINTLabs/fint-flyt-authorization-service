package no.fintlabs.flyt.azure.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ConfigUser {

    private static final List<String> userAttributes = Arrays.asList(
            "id",
            "mail"
    );

    public List<String> allAttributes() {
        return userAttributes;
    }

}