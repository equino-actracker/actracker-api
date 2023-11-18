package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Stream.concat;

public class IntegrationTestDashboardsConfiguration {

    private final List<DashboardDto> addedDashboards = new ArrayList<>();
    private final List<DashboardDto> transientDashboards = new ArrayList<>();

    void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addDashboards(addedDashboards.toArray(new DashboardDto[0]));
    }

    public void add(DashboardDto dashboard) {
        addedDashboards.add(dashboard);
    }

    public void addTransient(DashboardDto dashboard) {
        transientDashboards.add(dashboard);
    }

    public List<DashboardDto> accessibleFor(User user) {
        return addedDashboards
                .stream()
                .filter(not(DashboardDto::deleted))
                .filter(dashboard -> isOwnerOrGrantee(user, dashboard))
                .map(dashboard -> toAccessibleFormFor(user, dashboard))
                .sorted(comparing(dashboard -> dashboard.id().toString()))
                .toList();
    }

    private boolean isOwnerOrGrantee(User user, DashboardDto dashboard) {
        return isOwner(user, dashboard) || isGrantee(user, dashboard);
    }

    private boolean isGrantee(User user, DashboardDto dashboard) {
        List<User> grantees = dashboard.shares()
                .stream()
                .map(Share::grantee)
                .toList();
        return grantees.contains(user);
    }

    private boolean isOwner(User user, DashboardDto dashboard) {
        return user.id().equals(dashboard.creatorId());
    }

    public List<DashboardDto> inaccessibleFor(User user) {
        List<UUID> accessibleDashboards = accessibleFor(user)
                .stream()
                .map(DashboardDto::id)
                .toList();
        return concat(addedDashboards.stream(), transientDashboards.stream())
                .filter(dashboard -> !accessibleDashboards.contains(dashboard.id()))
                .toList();
    }

    private DashboardDto toAccessibleFormFor(User user, DashboardDto dashboard) {
        List<Share> shares = isOwner(user, dashboard)
                ? dashboard.shares()
                : emptyList();
        List<Chart> charts = dashboard.charts()
                .stream()
                .filter(not(Chart::isDeleted))
                .map(chart -> toAccessibleFormFor(user, chart))
                .toList();
        return new DashboardDto(
                dashboard.id(),
                dashboard.creatorId(),
                dashboard.name(),
                charts,
                shares,
                dashboard.deleted()
        );
    }

    private Chart toAccessibleFormFor(User user, Chart chart) {
        return new Chart(
                chart.id(),
                chart.name(),
                chart.groupBy(),
                chart.analysisMetric(),
                emptySet(), // TODO
                chart.isDeleted()
        );
    }
}
