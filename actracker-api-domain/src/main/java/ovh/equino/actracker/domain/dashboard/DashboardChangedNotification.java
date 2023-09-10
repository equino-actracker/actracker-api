package ovh.equino.actracker.domain.dashboard;

import java.util.UUID;

public record DashboardChangedNotification(
        DashboardDto dashboard
) {

    public UUID id() {
        return dashboard.id();
    }
}
