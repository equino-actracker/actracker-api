package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

class MetricValueMapper extends PayloadMapper {

    List<ovh.equino.actracker.domain.activity.MetricValue> fromRequest(Collection<MetricValue> metricValues) {
        return requireNonNullElse(metricValues, new ArrayList<MetricValue>())
                .stream()
                .map(this::fromRequest)
                .toList();
    }

    ovh.equino.actracker.domain.activity.MetricValue fromRequest(MetricValue metricValue) {
        return new ovh.equino.actracker.domain.activity.MetricValue(
                stringToUuid(metricValue.metricId()),
                metricValue.value()
        );
    }

    List<MetricValue> toResponse(List<ovh.equino.actracker.domain.activity.MetricValue> metricValues) {
        return requireNonNullElse(metricValues, new ArrayList<ovh.equino.actracker.domain.activity.MetricValue>())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    MetricValue toResponse(ovh.equino.actracker.domain.activity.MetricValue metricValue) {
        return new MetricValue(
                uuidToString(metricValue.metricId()),
                metricValue.value()
        );
    }

}
