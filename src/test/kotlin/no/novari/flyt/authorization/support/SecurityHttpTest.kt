package no.novari.flyt.authorization.support

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.sun.net.httpserver.HttpServer
import no.novari.flyt.security.UserRole
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Instant
import java.util.UUID

/** Signed NAM-like tokens through the app's real `JwtDecoder` and the filter chains of flyt-security-starter. */
@SpringBootTest(
    properties = [
        "fint.org-id=fintlabs.no",
        "novari.kafka.topic.org-id=fintlabs-no",
        "spring.kafka.admin.auto-create=false",
        "fint.flyt.authorization.sso.client-id=${SecurityHttpTest.INTERNAL_CLIENT_ID}",
        "novari.flyt.security.user.audience=${SecurityHttpTest.USER_AUDIENCE}",
    ],
)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
abstract class SecurityHttpTest {
    companion object {
        const val INTERNAL_CLIENT_ID = "fint-flyt-authorization-oauth2-client"
        const val USER_AUDIENCE = "fint-flyt-auth-forward-sso"
        private const val ISSUER = "https://idp.test/nidp/oauth/nam"

        private val key = RSAKeyGenerator(2048).keyID("test").generate()
        private val encoder = NimbusJwtEncoder(ImmutableJWKSet(JWKSet(key)))
        private val jwks =
            HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0).apply {
                createContext("/jwks") { exchange ->
                    val body = JWKSet(key.toPublicJWK()).toString().toByteArray()
                    exchange.responseHeaders.add("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, body.size.toLong())
                    exchange.responseBody.use { it.write(body) }
                }
                start()
            }

        // Shared by every test class, so the Spring context is reused.
        private val postgres = PostgreSQLContainer("postgres:17-alpine").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { ISSUER }
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") {
                "http://localhost:${jwks.address.port}/jwks"
            }
            registry.add("fint.database.url", postgres::getJdbcUrl)
            registry.add("fint.database.username", postgres::getUsername)
            registry.add("fint.database.password", postgres::getPassword)
            registry.add("spring.datasource.hikari.schema") { "public" }
        }

        /** NAM sets aud to the client that asked for the token, so aud is sub for client_credentials. */
        fun clientToken(clientId: String = INTERNAL_CLIENT_ID): String =
            sign { it.subject(clientId).audience(listOf(clientId)) }

        /** A FLYT user token. Tenant users are capped at USER, and operator orgs (vigo.no) at DEVELOPER. */
        fun userToken(
            role: UserRole = UserRole.USER,
            organizationId: String = "fintlabs.no",
            objectIdentifier: UUID = UUID.randomUUID(),
        ): String =
            sign {
                it
                    .subject("opaque-user-id")
                    .audience(listOf(USER_AUDIENCE))
                    .claim("organizationid", organizationId)
                    .claim("objectidentifier", objectIdentifier.toString())
                    .claim("roles", listOf(role.claimValue))
                    .claim("displayname", "Ada Lovelace")
            }

        fun developerToken(): String = userToken(UserRole.DEVELOPER, organizationId = "vigo.no")

        private fun sign(customize: (JwtClaimsSet.Builder) -> Unit): String {
            val now = Instant.now()
            val claims =
                JwtClaimsSet
                    .builder()
                    .issuer(ISSUER)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(300))
                    .claim("scope", "end-user-profile fint-api")
                    .also(customize)
                    .build()
            val header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(key.keyID).build()
            return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        }
    }
}
