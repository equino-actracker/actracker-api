package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.DashboardDto;

import java.util.UUID;

import static java.util.Objects.isNull;

class DashboardMapper {

    DashboardDto toDto(DashboardEntity entity) {

        return new DashboardDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.name,
                entity.deleted
        );
    }

    DashboardEntity toEntity(DashboardDto dto) {

        DashboardEntity entity = new DashboardEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.name = dto.name();
        entity.deleted = dto.deleted();
        return entity;
    }
}
