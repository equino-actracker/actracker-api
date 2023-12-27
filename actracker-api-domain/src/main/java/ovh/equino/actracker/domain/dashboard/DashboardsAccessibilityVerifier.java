package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

public interface DashboardsAccessibilityVerifier {
    boolean isAccessibleFor(User user, DashboardId dashboardId);
}
