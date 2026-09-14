package no.novari.flyt.authorization.openapi

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {
    @Bean
    fun authorizationOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("FINT Flyt Authorization Service API")
                    .description("Internal APIs for user authorization, permissions, and source applications.")
                    .version("v1"),
            ).components(
                Components().addSecuritySchemes(
                    BEARER_AUTH,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ),
            ).addSecurityItem(SecurityRequirement().addList(BEARER_AUTH))

    @Bean
    fun authorizationApiGroup(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("authorization")
            .pathsToMatch(
                "/api/intern/authorization/**",
                "/api/intern-klient/authorization/**",
            ).build()

    private companion object {
        private const val BEARER_AUTH = "bearerAuth"
    }
}
