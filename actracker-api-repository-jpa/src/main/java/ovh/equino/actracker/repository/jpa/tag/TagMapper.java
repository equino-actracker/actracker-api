package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.tag.TagDto;

import java.util.UUID;

import static java.util.Objects.isNull;

class TagMapper {

    private final MetricMapper metricMapper = new MetricMapper();

    TagDto toDto(TagEntity entity) {
        return new TagDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.name,
                metricMapper.toValueObjects(entity.metrics),
                entity.deleted
        );
    }

    TagEntity toEntity(TagDto dto) {
        TagEntity entity = new TagEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.name = dto.name();
        entity.metrics = metricMapper.toEntities(dto.metrics(), entity);
        entity.deleted = dto.deleted();
        return entity;
    }
}
