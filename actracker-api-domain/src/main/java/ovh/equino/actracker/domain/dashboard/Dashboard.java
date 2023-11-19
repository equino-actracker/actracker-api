package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;


public class Dashboard implements Entity {

    private final DashboardId id;
    private final User creator;
    private String name;
    final List<Chart> charts;
    final List<Share> shares;
    private boolean deleted;

    private final DashboardValidator validator;

    Dashboard(DashboardId id,
              User creator,
              String name,
              List<Chart> charts,
              List<Share> shares,
              boolean deleted,
              DashboardValidator validator) {

        this.id = id;
        this.creator = creator;
        this.name = name;
        this.charts = new ArrayList<>(charts);
        this.shares = new ArrayList<>(shares);
        this.deleted = deleted;

        this.validator = validator;
    }

    public static Dashboard create(DashboardDto dashboard, User creator) {
        Dashboard newDashboard = new Dashboard(
                new DashboardId(),
                creator,
                dashboard.name(),
                dashboard.charts(),
                dashboard.shares(),
                false,
                new DashboardValidator()
        );
        newDashboard.validate();
        return newDashboard;
    }

    public void rename(String newName, User editor) {
        new DashboardEditOperation(editor, this, () ->
                this.name = newName
        ).execute();
    }

    public void addChart(Chart newChart, User editor) {
        new DashboardEditOperation(editor, this, () ->
                charts.add(newChart)
        ).execute();
    }

    public void deleteChart(ChartId chartId, User editor) {
        new DashboardEditOperation(editor, this, () -> {

            List<Chart> deletedCharts = charts.stream()
                    .filter(chart -> chart.id().equals(chartId))
                    .map(Chart::deleted)
                    .toList();
            List<Chart> remainingCharts = charts.stream()
                    .filter(chart -> !chart.id().equals(chartId))
                    .toList();
            charts.clear();
            charts.addAll(deletedCharts);
            charts.addAll(remainingCharts);

        }).execute();
    }

    public void delete(User remover) {
        new DashboardEditOperation(remover, this, () -> {

            List<Chart> allChartsDeleted = this.charts.stream()
                    .map(Chart::deleted)
                    .toList();
            this.charts.clear();
            this.charts.addAll(allChartsDeleted);
            this.deleted = true;

        }).execute();
    }

    public void share(Share share, User granter) {
        new DashboardEditOperation(granter, this, () -> {

            List<String> existingGranteeNames = this.shares.stream()
                    .map(Share::granteeName)
                    .toList();
            if (!existingGranteeNames.contains(share.granteeName())) {
                this.shares.add(share);
            }

        }).execute();
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

    public static Dashboard fromStorage(DashboardDto dashboard) {
        return new Dashboard(
                new DashboardId(dashboard.id()),
                new User(dashboard.creatorId()),
                dashboard.name(),
                dashboard.charts(),
                dashboard.shares(),
                dashboard.deleted(),
                new DashboardValidator()
        );
    }

    public DashboardDto forStorage() {
        return new DashboardDto(
                id.id(), creator.id(), name, unmodifiableList(charts), unmodifiableList(shares), deleted
        );
    }

    public DashboardChangedNotification forChangeNotification() {
        DashboardDto dto = new DashboardDto(
                id.id(), creator.id(), name, unmodifiableList(charts), unmodifiableList(shares), deleted
        );
        return new DashboardChangedNotification(dto);
    }

    @Override
    public void validate() {
        validator.validate(this);
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

    // TODO think about extracting it to superclass
    public DashboardId id() {
        return this.id;
    }
}
