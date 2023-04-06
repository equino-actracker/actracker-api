package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;

import java.util.Collections;
import java.util.UUID;

import static java.util.Objects.isNull;

class ActivityMapper {

    ActivityDto toDto(ActivityEntity entity) {
        return new ActivityDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.startTime,
                entity.endTime,
                entity.comment,
                Collections.emptySet(), // TODO :eyes:
                entity.deleted
        );
    }

    ActivityEntity toEntity(ActivityDto dto) {
        ActivityEntity entity = new ActivityEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.startTime = dto.startTime();
        entity.endTime = dto.endTime();
        entity.comment = dto.comment();
        entity.deleted = dto.deleted();
        return entity;
    }
}
