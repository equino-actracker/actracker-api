package ovh.equino.actracker.domain.dashboard;

import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class DashboardEditOperationTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    private final DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier = null;

    @Test
    void shouldPreserveDeletedCharts() {
        // given
        Chart nonDeletedChart = new Chart("chart1", GroupBy.SELF, AnalysisMetric.METRIC_VALUE, emptySet());
        Chart deletedChart = new Chart("chart1", GroupBy.SELF, AnalysisMetric.METRIC_VALUE, emptySet()).deleted();
        Dashboard dashboard = new Dashboard(
                new DashboardId(),
                CREATOR,
                "dashboard",
                List.of(deletedChart, nonDeletedChart),
                emptyList(),
                !DELETED,
                dashboardsAccessibilityVerifier,
                new DashboardValidator()
        );
        DashboardEditOperation editOperation = new DashboardEditOperation(CREATOR, dashboard, () -> {
        });

        // when
        editOperation.beforeEditOperation();
        List<Chart> chartsDuringEdit = dashboard.charts.stream().toList();
        editOperation.afterEditOperation();

        // then
        assertThat(chartsDuringEdit).containsOnly(nonDeletedChart);
        assertThat(dashboard.charts).containsExactlyInAnyOrder(deletedChart, nonDeletedChart);
    }
}
