package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class ActivityMapper {

    private final MetricValueMapper metricValueMapper;

    public ActivityMapper(EntityManager entityManager) {
        this.metricValueMapper = new MetricValueMapper(entityManager);
    }

    // TODO delete
    ActivityDto toDto(ActivityEntity entity) {

        Set<UUID> entityTags = requireNonNullElse(entity.tags, new HashSet<TagEntity>()).stream()
                .map(tag -> tag.id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        return new ActivityDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.title,
                isNull(entity.startTime) ? null : entity.startTime.toInstant(),
                isNull(entity.endTime) ? null : entity.endTime.toInstant(),
                entity.comment,
                entityTags,
                metricValueMapper.toValueObjects(entity.metricValues),
                entity.deleted
        );
    }

    // TODO delete after domain events introduction
    ActivityEntity toEntity(ActivityDto dto) {

        Set<TagEntity> dtoTags = requireNonNullElse(dto.tags(), new HashSet<UUID>()).stream()
                .map(UUID::toString)
                .map(this::toTagEntity)
                .collect(toUnmodifiableSet());

        ActivityEntity entity = new ActivityEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.title = dto.title();
        entity.startTime = isNull(dto.startTime()) ? null : Timestamp.from(dto.startTime());
        entity.endTime = isNull(dto.endTime()) ? null : Timestamp.from(dto.endTime());
        entity.comment = dto.comment();
        entity.tags = dtoTags;
        entity.metricValues = metricValueMapper.toEntities(dto.metricValues(), entity);
        entity.deleted = dto.deleted();
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.id = tagId;
        return tagEntity;
    }
}
