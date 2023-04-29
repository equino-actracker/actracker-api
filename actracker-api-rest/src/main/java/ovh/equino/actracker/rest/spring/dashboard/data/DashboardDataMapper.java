package ovh.equino.actracker.rest.spring.dashboard.data;

import ovh.equino.actracker.rest.spring.PayloadMapper;

class DashboardDataMapper extends PayloadMapper {

    private final DashboardDataChartMapper chartMapper = new DashboardDataChartMapper();

    DashboardData toResponse(ovh.equino.actracker.domain.dashboard.DashboardData dashboardData) {
        return new DashboardData(
                dashboardData.name(),
                chartMapper.toResponse(dashboardData.charts())
        );
    }
}
