package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toUnmodifiableSet;

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

        Set<UUID> chartIds = chartResults
                .stream()
                .map(ChartJoinDashboardProjection::id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        SelectChartJoinTagQuery selectChartJoinTag = new SelectChartJoinTagQuery(entityManager);
        Map<String, Set<UUID>> tagIdsByChartId = selectChartJoinTag
                .where(selectChartJoinTag.predicate().and(
                                selectChartJoinTag.predicate().isNotDeleted(),
                                selectChartJoinTag.predicate().isAccessibleFor(searcher),
                                selectChartJoinTag.predicate().hasChartIdIn(chartIds)
                        )
                )
                .execute()
                .stream()
                .collect(Collectors.groupingBy(
                        ChartJoinTagProjection::chartId,
                        mapping(ChartJoinTagProjection::toTagId, toUnmodifiableSet())
                ));

        List<Chart> charts = chartResults
                .stream()
                .map(chart -> chart.toChart(tagIdsByChartId.getOrDefault(chart.id(), emptySet())))
                .toList();

        SelectShareJoinDashboardQuery selectShareJoinDashboard = new SelectShareJoinDashboardQuery(entityManager);
        List<Share> shares = selectShareJoinDashboard
                .where(
                        selectShareJoinDashboard.predicate().and(
                                selectShareJoinDashboard.predicate().isAccessibleFor(searcher),
                                selectShareJoinDashboard.predicate().hasDashboardId(dashboardId.id())
                        )
                )
                .execute()
                .stream()
                .map(ShareJoinDashboardProjection::toShare)
                .toList();

        return dashboardResult.map(result -> result.toDashboard(charts, shares));
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        throw new RuntimeException("Not implemented yet");
    }
}
