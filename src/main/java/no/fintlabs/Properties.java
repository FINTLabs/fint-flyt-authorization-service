package no.fintlabs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class Properties {

    @Value("fint.org-id")
    private String orgId;
}
