package no.novari.flyt.authorization

import no.novari.flyt.webresourceserver.security.AuthorityMappingService
import no.novari.flyt.webresourceserver.security.AuthorizationServiceConfiguration
import no.novari.flyt.webresourceserver.security.user.UserAuthorizationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.client.RestClient
import java.util.function.Supplier

class WebResourceServerCompatibilityTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    OAuth2ClientAutoConfiguration::class.java,
                    AuthorizationServiceConfiguration::class.java,
                ),
            ).withBean(AuthorityMappingService::class.java, Supplier { mock<AuthorityMappingService>() })
            .withBean(RestClient.Builder::class.java, Supplier { RestClient.builder() })
            .withPropertyValues(
                "spring.security.oauth2.client.registration.authorization-service.client-id=authorization-client",
                "spring.security.oauth2.client.registration.authorization-service.client-secret=secret",
                "spring.security.oauth2.client.registration.authorization-service.authorization-grant-type=client_credentials",
                "spring.security.oauth2.client.provider.authorization-service.token-uri=https://sso.example.no/token",
                "novari.flyt.web-resource-server.security.authorization.base-url=https://authorization.example.no",
            )

    @Test
    fun `loads web resource server authorization client configuration`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(ClientRegistrationRepository::class.java)
            assertThat(context).hasSingleBean(OAuth2AuthorizedClientManager::class.java)
            assertThat(context).hasSingleBean(UserAuthorizationService::class.java)
        }
    }
}
