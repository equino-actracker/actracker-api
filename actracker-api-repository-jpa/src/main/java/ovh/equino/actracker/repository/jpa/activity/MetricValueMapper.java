package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.repository.jpa.tag.MetricEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;

class MetricValueMapper {

    private final EntityManager entityManager;

    MetricValueMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    List<MetricValue> toDomainObjects(Collection<MetricValueEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<MetricValueEntity>())
                .stream()
                .map(this::toDomainObject)
                .toList();
    }

    MetricValue toDomainObject(MetricValueEntity entity) {
        return new MetricValue(
                UUID.fromString(entity.metric.id),
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
        entity.metric = findMetric(metricValue.metricId());
        entity.value = metricValue.value();
        return entity;
    }

    private MetricEntity findMetric(UUID metricId) {
        return entityManager.find(MetricEntity.class, metricId.toString());
    }
}
