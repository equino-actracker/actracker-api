package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;

import java.util.UUID;

import static java.util.Objects.isNull;

class ActivityMapper {

    ActivityDto toDto(ActivityEntity entity) {
        return new ActivityDto(
                UUID.fromString(entity.id),
                entity.startTime,
                entity.endTime
        );
    }

    ActivityEntity toEntity(ActivityDto dto) {
        ActivityEntity entity = new ActivityEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.startTime = dto.startTime();
        entity.endTime = dto.endTime();
        return entity;
    }
}
