package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

public class DashboardsAccessibilityVerifierImpl implements DashboardsAccessibilityVerifier {

    private final DashboardDataSource dashboardDataSource;

    DashboardsAccessibilityVerifierImpl(DashboardDataSource dashboardDataSource) {
        this.dashboardDataSource = dashboardDataSource;
    }

    @Override
    public boolean isAccessibleFor(User user, DashboardId dashboardId) {
        return dashboardDataSource.find(dashboardId, user).isPresent();
    }
}
