package ovh.equino.actracker.rest.spring.tag;

import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

class MetricMapper extends PayloadMapper {

    List<MetricDto> fromRequest(Collection<Metric> metrics) {
        return requireNonNullElse(metrics, new ArrayList<Metric>()).stream()
                .map(this::fromRequest)
                .toList();
    }

    MetricDto fromRequest(Metric metric) {
        return new MetricDto(
                stringToUuid(metric.id()),
                metric.name(),
                MetricType.valueOf(metric.type())
        );
    }

    List<Metric> toResponse(Collection<MetricDto> metrics) {
        return requireNonNullElse(metrics, new ArrayList<MetricDto>()).stream()
                .map(this::toResponse)
                .toList();
    }

    Metric toResponse(MetricDto metric) {
        return new Metric(
                uuidToString(metric.id()),
                metric.name(),
                metric.type().toString()
        );
    }
}
