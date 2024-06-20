package no.fintlabs.flyt.azure;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

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
