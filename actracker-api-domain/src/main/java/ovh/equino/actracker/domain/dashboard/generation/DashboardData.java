package ovh.equino.actracker.domain.dashboard.generation;

import java.util.Collection;

public record DashboardData(
        String name,
        Collection<DashboardChartData> charts
) {
}
