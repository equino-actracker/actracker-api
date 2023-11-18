package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        return addedDashboards;
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
}
