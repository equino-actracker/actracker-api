package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.jpa.activity.ActivityEntity;
import ovh.equino.actracker.jpa.activity.MetricValueEntity;
import ovh.equino.actracker.jpa.tag.MetricEntity;

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
                UUID.fromString(entity.getMetric().getId()),
                entity.getValue()
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
        entity.setId(randomUUID().toString());
        entity.setActivity(activity);
        entity.setMetric(findMetric(metricValue.metricId()));
        entity.setValue(metricValue.value());
        return entity;
    }

    private MetricEntity findMetric(UUID metricId) {
        return entityManager.find(MetricEntity.class, metricId.toString());
    }
}
