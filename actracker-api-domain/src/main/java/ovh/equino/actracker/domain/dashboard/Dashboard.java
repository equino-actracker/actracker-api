package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;


public final class Dashboard implements Entity {

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
              Collection<Chart> charts,
              Collection<Share> shares,
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

    public void rename(String newName, User editor) {
        if (!creator.equals(editor) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        if (!this.isEditableFor(editor)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        this.name = newName;
        this.validate();
    }

    public void addChart(Chart newChart, User editor) {
        if (!creator.equals(editor) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        if (!this.isEditableFor(editor)) {
            throw new EntityEditForbidden(Dashboard.class);
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
        charts.add(newChart);
        this.validate();
    }

    public void deleteChart(ChartId chartId, User editor) {
        if (!creator.equals(editor) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        if (!this.isEditableFor(editor)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        List<Chart> matchingDeletedCharts = charts.stream()
                .filter(chart -> chart.id().equals(chartId))
                .map(Chart::deleted)
                .toList();
        List<Chart> remainingCharts = charts.stream()
                .filter(chart -> !chart.id().equals(chartId))
                .toList();

        charts.clear();
        charts.addAll(matchingDeletedCharts);
        charts.addAll(remainingCharts);
        this.validate();
    }

    public void delete(User remover) {
        if (!creator.equals(remover) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        if (!this.isEditableFor(remover)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        List<Chart> allChartsDeleted = this.charts.stream()
                .map(Chart::deleted)
                .toList();
        this.charts.clear();
        this.charts.addAll(allChartsDeleted);
        this.deleted = true;
        this.validate();
    }

    public void share(Share share, User granter) {
        if (!creator.equals(granter) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        if (!this.isEditableFor(granter)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        List<String> existingGranteeNames = this.shares.stream()
                .map(Share::granteeName)
                .toList();
        if (!existingGranteeNames.contains(share.granteeName())) {
            this.shares.add(share);
        }
        this.validate();
    }

    public void unshare(String granteeName, User granter) {
        if (!creator.equals(granter) && !dashboardsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Dashboard.class, id.id());
        }
        if (!this.isEditableFor(granter)) {
            throw new EntityEditForbidden(Dashboard.class);
        }
        List<Share> sharesWithExclusion = this.shares.stream()
                .filter(share -> !share.granteeName().equals(granteeName))
                .toList();
        this.shares.clear();
        this.shares.addAll(sharesWithExclusion);
        this.validate();
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

    List<Chart> charts() {
        return unmodifiableList(charts);
    }

    List<Share> shares() {
        return unmodifiableList(shares);
    }

    @Override
    public User creator() {
        return creator;
    }

    public boolean deleted() {
        return deleted;
    }

    // TODO think about extracting it to superclass
    public DashboardId id() {
        return this.id;
    }
}
