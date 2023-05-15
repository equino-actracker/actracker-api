package ovh.equino.actracker.rest.spring.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

class MetricMapper {

    List<ovh.equino.actracker.domain.metric.Metric> fromRequest(Collection<Metric> metrics) {
        return requireNonNullElse(metrics, new ArrayList<Metric>()).stream()
                .map(this::fromRequest)
                .toList();
    }

    ovh.equino.actracker.domain.metric.Metric fromRequest(Metric metric) {
        return new ovh.equino.actracker.domain.metric.Metric(
                metric.name(),
                Metric.MetricType.toDomain(metric.type())
        );
    }

    List<Metric> toResponse(Collection<ovh.equino.actracker.domain.metric.Metric> metrics) {
        return requireNonNullElse(metrics, new ArrayList<ovh.equino.actracker.domain.metric.Metric>()).stream()
                .map(this::toResponse)
                .toList();
    }

    Metric toResponse(ovh.equino.actracker.domain.metric.Metric metric) {
        return new Metric(
                metric.name(),
                Metric.MetricType.fromDomain(metric.type())
        );
    }
}
