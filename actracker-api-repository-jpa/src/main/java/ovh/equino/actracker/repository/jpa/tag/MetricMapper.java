package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.MetricEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;

class MetricMapper {

    private final MetricFactory metricFactory;

    MetricMapper(MetricFactory metricFactory) {
        this.metricFactory = metricFactory;
    }

    List<Metric> toDomainObjects(Collection<MetricEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<MetricEntity>())
                .stream()
                .map(this::toDomainObject)
                .toList();
    }

    Metric toDomainObject(MetricEntity entity) {
        if (isNull(entity)) {
            return null;
        }
        return metricFactory.reconstitute(
                new MetricId(entity.getId()),
                new User(entity.getCreatorId()),
                entity.getName(),
                MetricType.valueOf(entity.getType()),
                entity.isDeleted()
        );
    }

    List<MetricEntity> toEntities(Collection<MetricDto> metrics, TagEntity tag) {
        return requireNonNullElse(metrics, new ArrayList<MetricDto>()).stream()
                .map(metric -> toEntity(metric, tag))
                .toList();
    }

    MetricEntity toEntity(MetricDto metric, TagEntity tagEntity) {
        MetricEntity entity = new MetricEntity();
        entity.setId(metric.id().toString());
        entity.setCreatorId(metric.creatorId().toString());
        entity.setName(metric.name());
        entity.setTag(tagEntity);
        entity.setType(metric.type().toString());
        entity.setDeleted(metric.deleted());
        return entity;
    }
}
