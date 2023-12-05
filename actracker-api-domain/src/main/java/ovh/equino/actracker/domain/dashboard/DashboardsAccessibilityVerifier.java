package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

public class DashboardsAccessibilityVerifier {

    private final DashboardDataSource dashboardDataSource;
    private final User user;

    public DashboardsAccessibilityVerifier(DashboardDataSource dashboardDataSource, User user) {
        this.dashboardDataSource = dashboardDataSource;
        this.user = user;
    }

    boolean isAccessible(DashboardId dashboardId) {
        return dashboardDataSource.find(dashboardId, user).isPresent();
    }
}
