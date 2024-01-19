package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.Dashboard;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardFactory;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;

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
                new DashboardId(entity.getId()),
                new User(entity.getCreatorId()),
                entity.getName(),
                chartMapper.toDomainObjects(entity.getCharts()),
                shareMapper.toDomainObjects(entity.getShares()),
                entity.isDeleted()
        );
    }

    DashboardEntity toEntity(DashboardDto dto) {

        DashboardEntity entity = new DashboardEntity();
        entity.setId(isNull(dto.id()) ? null : dto.id().toString());
        entity.setCreatorId(isNull(dto.creatorId()) ? null : dto.creatorId().toString());
        entity.setName(dto.name());
        entity.setCharts(chartMapper.toEntities(dto.charts(), entity));
        entity.setShares(shareMapper.toEntities(dto.shares(), entity));
        entity.setDeleted(dto.deleted());
        return entity;
    }
}
