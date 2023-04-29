package ovh.equino.actracker.rest.spring.dashboard.data;

import java.util.Collection;

record DashboardData(
        Collection<DashboardDataChart> charts
) {
}
