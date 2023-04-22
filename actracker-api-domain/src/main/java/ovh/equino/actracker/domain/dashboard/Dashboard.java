package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.user.User;


class Dashboard implements Entity {

    private final DashboardId id;
    private final User creator;
    private String name;
    private boolean deleted;

    private Dashboard(DashboardId id, User creator, String name, boolean deleted) {
        this.id = id;
        this.creator = creator;
        this.name = name;
        this.deleted = deleted;
    }

    static Dashboard create(DashboardDto dashboard, User creator) {
        Dashboard newDashboard = new Dashboard(
                new DashboardId(),
                creator,
                dashboard.name(),
                false
        );
        newDashboard.validate();
        return newDashboard;
    }

    void updateTo(DashboardDto dashboard) {
        this.name = dashboard.name();
        this.validate();
    }

    void delete() {
        this.deleted = true;
    }

    static Dashboard fromStorage(DashboardDto dashboard) {
        return new Dashboard(
                new DashboardId(dashboard.id()),
                new User(dashboard.creatorId()),
                dashboard.name(),
                dashboard.deleted()
        );
    }

    DashboardDto forStorage() {
        return new DashboardDto(id.id(), creator.id(), name, deleted);
    }

    DashboardDto forClient() {
        return new DashboardDto(id.id(), creator.id(), name, deleted);
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    @Override
    public void validate() {
        new DashboardValidator(this).validate();
    }

    String name() {
        return this.name;
    }
}
