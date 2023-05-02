package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.*;

import java.util.List;

class RepositoryDashboardGenerationEngine implements DashboardGenerationEngine {

    private final DashboardRepository dashboardRepository;

    RepositoryDashboardGenerationEngine(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationCriteria generationCriteria) {
        List<DashboardChartData> chartsData = dashboard.charts().stream()
                .map(chart -> generate(chart, generationCriteria))
                .toList();

        return new DashboardData(dashboard.name(), chartsData);
    }

    private DashboardChartData generate(Chart chart, DashboardGenerationCriteria generationCriteria) {
        return switch (chart.groupBy()) {
            case TAG -> dashboardRepository.generateChartGroupedByTags(chart.name(), generationCriteria);
            case DAY -> dashboardRepository.generateChartGroupedByDays(chart.name(), generationCriteria);
        };
    }
}
