package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

class MetricMapper {

    List<MetricDto> toDto(Collection<MetricEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<MetricEntity>()).stream()
                .map(this::toDto)
                .toList();
    }

    MetricDto toDto(MetricEntity entity) {
        return new MetricDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.name,
                MetricType.valueOf(entity.type),
                entity.deleted
        );
    }

    List<MetricEntity> toEntities(Collection<MetricDto> metrics, TagEntity tag) {
        return requireNonNullElse(metrics, new ArrayList<MetricDto>()).stream()
                .map(metric -> toEntity(metric, tag))
                .toList();
    }

    MetricEntity toEntity(MetricDto metric, TagEntity tagEntity) {
        MetricEntity entity = new MetricEntity();
        entity.id = metric.id().toString();
        entity.creatorId = metric.creatorId().toString();
        entity.name = metric.name();
        entity.tag = tagEntity;
        entity.type = metric.type().toString();
        entity.deleted = metric.deleted();
        return entity;
    }
}
