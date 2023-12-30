package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.Dashboard;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardFactory;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

import static java.util.Objects.isNull;

class DashboardMapper {

    private final DashboardFactory dashboardFactory;
    private final ChartMapper chartMapper;
    private final DashboardShareMapper shareMapper;

    DashboardMapper(DashboardFactory dashboardFactory) {
        this.dashboardFactory = dashboardFactory;
        this.chartMapper = new ChartMapper();
        this.shareMapper = new DashboardShareMapper();
    }

    Dashboard toDomainObject(DashboardEntity entity) {
        if (isNull(entity)) {
            return null;
        }
        return dashboardFactory.reconstitute(
                new DashboardId(entity.id),
                new User(entity.creatorId),
                entity.name,
                chartMapper.toDomainObjects(entity.charts),
                shareMapper.toDomainObjects(entity.shares),
                entity.deleted
        );
    }

    DashboardDto toDto(DashboardEntity entity) {

        return new DashboardDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.name,
                chartMapper.toDomainObjects(entity.charts),
                shareMapper.toDomainObjects(entity.shares),
                entity.deleted
        );
    }

    DashboardEntity toEntity(DashboardDto dto) {

        DashboardEntity entity = new DashboardEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.name = dto.name();
        entity.charts = chartMapper.toEntities(dto.charts(), entity);
        entity.shares = shareMapper.toEntities(dto.shares(), entity);
        entity.deleted = dto.deleted();
        return entity;
    }
}
