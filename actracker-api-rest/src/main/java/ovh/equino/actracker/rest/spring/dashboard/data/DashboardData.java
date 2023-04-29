package ovh.equino.actracker.rest.spring.dashboard.data;

import java.util.Collection;

record DashboardData(
        String name,
        Collection<DashboardDataChart> charts
) {
}
