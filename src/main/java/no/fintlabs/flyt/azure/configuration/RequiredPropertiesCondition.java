package no.fintlabs.flyt.azure.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

// TODO eivindmorch 27/06/2024 : Kan dette gjøres med bean-avhengighet?
public class RequiredPropertiesCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        String clientId = context.getEnvironment().getProperty("azure.credentials.clientid");
        String clientSecret = context.getEnvironment().getProperty("azure.credentials.clientsecret");
        String tenantId = context.getEnvironment().getProperty("azure.credentials.tenantid");
        String appId = context.getEnvironment().getProperty("azure.credentials.appid");

        return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret)
                && StringUtils.hasText(tenantId) && StringUtils.hasText(appId);
    }
}
