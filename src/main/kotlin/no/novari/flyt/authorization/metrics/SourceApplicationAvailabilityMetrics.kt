package no.novari.flyt.authorization.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import no.novari.flyt.authorization.client.sourceapplications.model.SourceApplication
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SourceApplicationAvailabilityMetrics(
    private val sourceApplications: List<SourceApplication>,
    private val environment: Environment,
) : MeterBinder {
    override fun bindTo(registry: MeterRegistry) {
        val orgId = resolveOrgId()

        sourceApplications
            .asSequence()
            .sortedBy(SourceApplication::id)
            .forEach { sourceApplication ->
                Gauge
                    .builder(INFO_METRIC_NAME) { 1.0 }
                    .description("Source application metadata for the configured organization")
                    .tag("org_id", orgId)
                    .tag("sourceapplication_id", sourceApplication.id.toString())
                    .tag("source_application", sourceApplication.displayName)
                    .register(registry)
            }

        sourceApplications
            .asSequence()
            .filter(SourceApplication::available)
            .sortedBy(SourceApplication::id)
            .forEach { sourceApplication ->
                Gauge
                    .builder(AVAILABILITY_METRIC_NAME) { 1.0 }
                    .description("Source applications enabled for the configured organization")
                    .tag("org_id", orgId)
                    .tag("sourceapplication_id", sourceApplication.id.toString())
                    .tag("source_application", sourceApplication.displayName)
                    .register(registry)
            }
    }

    private fun resolveOrgId(): String {
        return environment.getProperty("fint.org-id")
            ?: environment.getProperty("novari.kafka.topic.org-id")
            ?: environment.getProperty("novari.kafka.topic.orgId")
            ?: "unknown"
    }

    companion object {
        const val AVAILABILITY_METRIC_NAME: String = "flyt.authorization.source.application.available"
        const val INFO_METRIC_NAME: String = "flyt.authorization.source.application.info"
    }
}
