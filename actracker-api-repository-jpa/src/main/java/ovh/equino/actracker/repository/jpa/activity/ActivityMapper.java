package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.activity.Activity;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityFactory;
import ovh.equino.actracker.domain.activity.ActivityId;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.activity.ActivityEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class ActivityMapper {

    private final ActivityFactory activityFactory;
    private final MetricValueMapper metricValueMapper;

    public ActivityMapper(ActivityFactory activityFactory, EntityManager entityManager) {
        this.metricValueMapper = new MetricValueMapper(entityManager);
        this.activityFactory = activityFactory;
    }

    Activity toDomainObject(ActivityEntity entity) {
        if (isNull(entity)) {
            return null;
        }
        Set<TagId> tags = requireNonNullElse(entity.getTags(), new HashSet<TagEntity>())
                .stream()
                .map(tag -> new TagId(tag.getId()))
                .collect(toUnmodifiableSet());

        return activityFactory.reconstitute(
                new ActivityId(entity.getId()),
                new User(entity.getCreatorId()),
                entity.getTitle(),
                isNull(entity.getStartTime()) ? null : entity.getStartTime().toInstant(),
                isNull(entity.getEndTime()) ? null : entity.getEndTime().toInstant(),
                entity.getComment(),
                tags,
                metricValueMapper.toDomainObjects(entity.getMetricValues()),
                entity.isDeleted()
        );
    }

    ActivityEntity toEntity(ActivityDto dto) {

        Set<TagEntity> dtoTags = requireNonNullElse(dto.tags(), new HashSet<UUID>()).stream()
                .map(UUID::toString)
                .map(this::toTagEntity)
                .collect(toUnmodifiableSet());

        ActivityEntity entity = new ActivityEntity();
        entity.setId(isNull(dto.id()) ? null : dto.id().toString());
        entity.setCreatorId(isNull(dto.creatorId()) ? null : dto.creatorId().toString());
        entity.setTitle(dto.title());
        entity.setStartTime(isNull(dto.startTime()) ? null : Timestamp.from(dto.startTime()));
        entity.setEndTime(isNull(dto.endTime()) ? null : Timestamp.from(dto.endTime()));
        entity.setComment(dto.comment());
        entity.setTags(dtoTags);
        entity.setMetricValues(metricValueMapper.toEntities(dto.metricValues(), entity));
        entity.setDeleted(dto.deleted());
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.setId(tagId);
        return tagEntity;
    }
}
