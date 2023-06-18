package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;


class Dashboard implements Entity {

    private final DashboardId id;
    private final User creator;
    private String name;
    private final List<Chart> charts;
    private final List<Share> shares;
    private boolean deleted;

    private Dashboard(DashboardId id,
                      User creator,
                      String name,
                      List<Chart> charts,
                      List<Share> shares,
                      boolean deleted) {

        this.id = id;
        this.creator = creator;
        this.name = name;
        this.charts = new ArrayList<>(charts);
        this.shares = new ArrayList<>(shares);
        this.deleted = deleted;
    }

    static Dashboard create(DashboardDto dashboard, User creator) {
        Dashboard newDashboard = new Dashboard(
                new DashboardId(),
                creator,
                dashboard.name(),
                dashboard.charts(),
                emptyList(),
                false
        );
        newDashboard.validate();
        return newDashboard;
    }

    void updateTo(DashboardDto dashboard) {
        this.name = dashboard.name();
        this.charts.clear();
        this.charts.addAll(dashboard.charts());
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
                dashboard.charts(),
                dashboard.shares(),
                dashboard.deleted()
        );
    }

    DashboardDto forStorage() {
        return new DashboardDto(
                id.id(), creator.id(), name, unmodifiableList(charts), unmodifiableList(shares), deleted
        );
    }

    DashboardDto forClient() {
        return new DashboardDto(
                id.id(), creator.id(), name, unmodifiableList(charts), unmodifiableList(shares), deleted
        );
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user) || isGrantee(user);
    }

    private boolean isGrantee(User user) {
        return shares.stream()
                .map(Share::grantee)
                .filter(Objects::nonNull)
                .anyMatch(Predicate.isEqual(user));
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    void share(Share share) {
        List<String> existingGranteeNames = this.shares.stream()
                .map(Share::granteeName)
                .toList();
        if (!existingGranteeNames.contains(share.granteeName())) {
            this.shares.add(share);
        }
    }

    @Override
    public void validate() {
        new DashboardValidator(this).validate();
    }

    String name() {
        return this.name;
    }
}
