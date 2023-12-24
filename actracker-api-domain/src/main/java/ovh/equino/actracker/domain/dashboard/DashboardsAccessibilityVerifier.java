package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

public class DashboardsAccessibilityVerifier {

    private final DashboardDataSource dashboardDataSource;

    DashboardsAccessibilityVerifier(DashboardDataSource dashboardDataSource) {
        this.dashboardDataSource = dashboardDataSource;
    }

    boolean isAccessibleFor(User user, DashboardId dashboardId) {
        return dashboardDataSource.find(dashboardId, user).isPresent();
    }
}
