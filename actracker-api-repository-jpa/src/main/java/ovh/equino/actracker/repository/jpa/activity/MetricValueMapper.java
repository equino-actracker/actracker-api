package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.domain.activity.MetricValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;

class MetricValueMapper {

    List<MetricValue> toValueObjects(Collection<MetricValueEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<MetricValueEntity>())
                .stream()
                .map(this::toValueObject)
                .toList();
    }

    MetricValue toValueObject(MetricValueEntity entity) {
        return new MetricValue(
                UUID.fromString(entity.metricId),
                entity.value
        );
    }

    List<MetricValueEntity> toEntities(Collection<MetricValue> metricValues, ActivityEntity activity) {
        return requireNonNullElse(metricValues, new ArrayList<MetricValue>())
                .stream()
                .map(metricValue -> toEntity(metricValue, activity))
                .toList();
    }

    MetricValueEntity toEntity(MetricValue metricValue, ActivityEntity activity) {
        MetricValueEntity entity = new MetricValueEntity();
        entity.id = randomUUID().toString();
        entity.activity = activity;
        entity.metricId = metricValue.metricId().toString();
        entity.value = metricValue.value();
        return entity;
    }
}
