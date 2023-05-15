package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.metric.Metric;
import ovh.equino.actracker.domain.metric.MetricType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;

class MetricMapper {

    List<Metric> toValueObjects(Collection<MetricEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<MetricEntity>()).stream()
                .map(this::toValueObject)
                .toList();

    }

    Metric toValueObject(MetricEntity entity) {
        return new Metric(entity.name, MetricType.valueOf(entity.type));
    }

    List<MetricEntity> toEntities(Collection<Metric> metrics, TagEntity tag) {
        return requireNonNullElse(metrics, new ArrayList<Metric>()).stream()
                .map(metric -> toEntity(metric, tag))
                .toList();
    }

    MetricEntity toEntity(Metric metric, TagEntity tagEntity) {
        MetricEntity entity = new MetricEntity();
        entity.id = randomUUID().toString();
        entity.name = metric.name();
        entity.tag = tagEntity;
        return entity;
    }
}
