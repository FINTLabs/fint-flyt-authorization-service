package no.novari.flyt.authorization.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.novari.flyt.authorization.client.sourceapplications.model.AcosSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.DigisakSourceApplication
import no.novari.flyt.authorization.client.sourceapplications.model.VigoSourceApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class SourceApplicationAvailabilityMetricsTest {
    @Test
    fun `bindTo registers metrics for available source applications only`() {
        val registry = SimpleMeterRegistry()
        val metrics =
            SourceApplicationAvailabilityMetrics(
                sourceApplications =
                    listOf(
                        AcosSourceApplication("acos-client-id", true),
                        DigisakSourceApplication("digisak-client-id", false),
                        VigoSourceApplication("vigo-client-id", true),
                    ),
                environment = MockEnvironment().withProperty("fint.org-id", "afk.no"),
            )

        metrics.bindTo(registry)

        val allGauges = registry.find(SourceApplicationAvailabilityMetrics.METRIC_NAME).gauges()
        assertEquals(2, allGauges.size)

        val acosGauge =
            registry
                .find(SourceApplicationAvailabilityMetrics.METRIC_NAME)
                .tags(
                    "org_id",
                    "afk.no",
                    "source_application",
                    "Acos Interact",
                ).gauge()

        val vigoGauge =
            registry
                .find(SourceApplicationAvailabilityMetrics.METRIC_NAME)
                .tags(
                    "org_id",
                    "afk.no",
                    "source_application",
                    "VIGO",
                ).gauge()

        val digisakGauge =
            registry
                .find(SourceApplicationAvailabilityMetrics.METRIC_NAME)
                .tags(
                    "org_id",
                    "afk.no",
                    "source_application",
                    "Digisak",
                ).gauge()

        assertNotNull(acosGauge)
        assertEquals(1.0, acosGauge?.value())
        assertNotNull(vigoGauge)
        assertEquals(1.0, vigoGauge?.value())
        assertNull(digisakGauge)
    }
}
