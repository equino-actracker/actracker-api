package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;
import static java.util.function.Predicate.isEqual;


class Dashboard implements Entity {

    private final DashboardId id;
    private final User creator;
    private String name;
    private final List<Chart> charts;
    final List<Share> shares;
    private boolean deleted;

    Dashboard(DashboardId id,
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
                dashboard.shares(),
                false
        );
        newDashboard.validate();
        return newDashboard;
    }

    public void rename(String newName, User editor) {
        new DashboardEditOperation(editor, this,
                () -> this.name = newName
        ).execute();
    }

    public void delete(User remover) {
        new DashboardEditOperation(remover, this,
                () -> this.deleted = true
        ).execute();
    }

    public void share(Share share, User granter) {
        if (isEditForbiddenFor(granter)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        List<String> existingGranteeNames = this.shares.stream()
                .map(Share::granteeName)
                .toList();
        if (!existingGranteeNames.contains(share.granteeName())) {
            this.shares.add(share);
        }
    }

    public void unshare(String granteeName, User granter) {
        new DashboardEditOperation(granter, this, () -> {

            List<Share> sharesWithExclusion = this.shares.stream()
                    .filter(share -> !share.granteeName().equals(granteeName))
                    .toList();
            this.shares.clear();
            this.shares.addAll(sharesWithExclusion);

        }).execute();
    }

    void updateTo(DashboardDto dashboard, User updater) {
        if (isEditForbiddenFor(updater)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        this.name = dashboard.name();
        this.charts.clear();
        this.charts.addAll(dashboard.charts());
        this.validate();
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

    boolean isAccessibleFor(User user) {
        return creator.equals(user) || isGrantee(user);
    }

    boolean isNotAccessibleFor(User user) {
        return !isAccessibleFor(user);
    }

    private boolean isEditForbiddenFor(User user) {
        return !creator.equals(user);
    }

    private boolean isGrantee(User user) {
        return shares.stream()
                .map(Share::grantee)
                .filter(Objects::nonNull)
                .anyMatch(isEqual(user));
    }

    @Override
    public void validate() {
        new DashboardValidator(this).validate();
    }

    String name() {
        return this.name;
    }

    @Override
    public User creator() {
        return creator;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
