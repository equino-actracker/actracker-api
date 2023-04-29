package ovh.equino.actracker.domain.dashboard;

import java.util.Collection;

public record DashboardData(
        String name,
        Collection<DashboardChartData> charts
) {
}
