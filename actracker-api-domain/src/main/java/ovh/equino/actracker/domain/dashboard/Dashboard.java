package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;


public class Dashboard implements Entity {

    private final DashboardId id;
    private final User creator;
    private String name;
    final List<Chart> charts;
    final List<Share> shares;
    private boolean deleted;

    private final DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final DashboardValidator validator;

    Dashboard(DashboardId id,
              User creator,
              String name,
              List<Chart> charts,
              List<Share> shares,
              boolean deleted,
              DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier,
              TagsAccessibilityVerifier tagsAccessibilityVerifier,
              DashboardValidator validator) {

        this.id = id;
        this.creator = creator;
        this.name = name;
        this.charts = new ArrayList<>(charts);
        this.shares = new ArrayList<>(shares);
        this.deleted = deleted;

        this.dashboardsAccessibilityVerifier = dashboardsAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.validator = validator;
    }

    public static Dashboard create(DashboardDto dashboard,
                                   User creator,
                                   DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier,
                                   TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        Dashboard newDashboard = new Dashboard(
                new DashboardId(),
                creator,
                dashboard.name(),
                dashboard.charts(),
                dashboard.shares(),
                false,
                dashboardsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                new DashboardValidator()
        );
        newDashboard.validate();
        return newDashboard;
    }

    public void rename(String newName, User editor) {
        if (!creator.equals(editor) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        new DashboardEditOperation(editor, this, () ->
                this.name = newName
        ).execute();
    }

    public void addChart(Chart newChart, User editor) {
        if (!creator.equals(editor) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        Set<TagId> includedTags = newChart.includedTags()
                .stream()
                .map(TagId::new)
                .collect(toUnmodifiableSet());
        tagsAccessibilityVerifier.nonAccessibleOf(includedTags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleTag) -> {
                    String errorMessage = "Tag with ID %s not found".formatted(inaccessibleTag.id());
                    throw new EntityInvalidException(Dashboard.class, errorMessage);
                });
        new DashboardEditOperation(editor, this, () ->
                charts.add(newChart)
        ).execute();
    }

    public void deleteChart(ChartId chartId, User editor) {
        if (!creator.equals(editor) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
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
        if (!creator.equals(remover) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
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
        if (!creator.equals(granter) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
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
        if (!creator.equals(granter) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        new DashboardEditOperation(granter, this, () -> {

            List<Share> sharesWithExclusion = this.shares.stream()
                    .filter(share -> !share.granteeName().equals(granteeName))
                    .toList();
            this.shares.clear();
            this.shares.addAll(sharesWithExclusion);

        }).execute();
    }

    public static Dashboard fromStorage(DashboardDto dashboard,
                                        DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier,
                                        TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        return new Dashboard(
                new DashboardId(dashboard.id()),
                new User(dashboard.creatorId()),
                dashboard.name(),
                dashboard.charts(),
                dashboard.shares(),
                dashboard.deleted(),
                dashboardsAccessibilityVerifier,
                tagsAccessibilityVerifier,
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
