package ovh.equino.actracker.domain.dashboard;

public interface DashboardNotifier {

    void notifyChanged(DashboardChangedNotification dashboardChangedNotification);
}
