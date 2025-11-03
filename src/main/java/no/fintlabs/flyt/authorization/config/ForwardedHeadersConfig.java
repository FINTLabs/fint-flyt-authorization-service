package no.fintlabs.flyt.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

@Configuration
public class ForwardedHeadersConfig {

    @Bean
    ForwardedHeaderTransformer forwardedHeaderTransformer() {
        ForwardedHeaderTransformer transformer = new ForwardedHeaderTransformer();
        transformer.setRemoveOnly(false);
        return transformer;
    }
}
