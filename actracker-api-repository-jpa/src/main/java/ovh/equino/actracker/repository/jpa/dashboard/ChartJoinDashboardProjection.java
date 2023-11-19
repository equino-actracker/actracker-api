package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.AnalysisMetric;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartId;
import ovh.equino.actracker.domain.dashboard.GroupBy;

import java.util.Set;
import java.util.UUID;

record ChartJoinDashboardProjection(String id,
                                    String dashboardId,
                                    String name,
                                    String groupBy,
                                    String analysisMetric,
                                    Boolean deleted) {

    Chart toChart(Set<UUID> tagIds) {
        return new Chart(
                new ChartId(id()),
                name(),
                GroupBy.valueOf(groupBy),
                AnalysisMetric.valueOf(analysisMetric),
                tagIds,
                deleted
        );
    }
}
