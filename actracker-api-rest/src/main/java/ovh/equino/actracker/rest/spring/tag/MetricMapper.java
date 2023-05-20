package ovh.equino.actracker.rest.spring.tag;

import ovh.equino.actracker.domain.tag.MetricDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

class MetricMapper {

    List<MetricDto> fromRequest(Collection<Metric> metrics) {
        return requireNonNullElse(metrics, new ArrayList<Metric>()).stream()
                .map(this::fromRequest)
                .toList();
    }

    MetricDto fromRequest(Metric metric) {
        return new MetricDto(
                metric.name(),
                Metric.MetricType.toDomain(metric.type())
        );
    }

    List<Metric> toResponse(Collection<MetricDto> metrics) {
        return requireNonNullElse(metrics, new ArrayList<MetricDto>()).stream()
                .map(this::toResponse)
                .toList();
    }

    Metric toResponse(MetricDto metric) {
        return new Metric(
                metric.name(),
                Metric.MetricType.fromDomain(metric.type())
        );
    }
}
