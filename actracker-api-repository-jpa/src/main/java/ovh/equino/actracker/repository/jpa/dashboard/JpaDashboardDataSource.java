package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class JpaDashboardDataSource extends JpaDAO implements DashboardDataSource {

    JpaDashboardDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<DashboardDto> find(DashboardId dashboardId, User searcher) {

        SelectDashboardQuery selectDashboard = new SelectDashboardQuery(entityManager);
        Optional<DashboardProjection> dashboardResult = selectDashboard
                .where(
                        selectDashboard.predicate().and(
                                selectDashboard.predicate().hasId(dashboardId.id()),
                                selectDashboard.predicate().isNotDeleted(),
                                selectDashboard.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute();

        SelectChartJoinDashboardQuery selectChartJoinDashboard = new SelectChartJoinDashboardQuery(entityManager);
        List<ChartJoinDashboardProjection> chartResults = selectChartJoinDashboard
                .where(
                        selectChartJoinDashboard.predicate().and(
                                selectChartJoinDashboard.predicate().hasDashboardId(dashboardId.id()),
                                selectChartJoinDashboard.predicate().isNotDeleted()
                        )
                )
                .execute();

        List<Chart> charts = chartResults
                .stream()
                .map(chart -> chart.toChart(Collections.emptySet()))
                .toList();

        return dashboardResult.map(result -> result.toDashboard(charts, Collections.emptyList()));
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        throw new RuntimeException("Not implemented yet");
    }
}
