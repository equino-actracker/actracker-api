package ovh.equino.actracker.repository.jpa.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.dashboard.Dashboard;
import ovh.equino.actracker.domain.dashboard.DashboardFactory;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.dashboard.DashboardTestFactory;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;

abstract class JpaDashboardRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaDashboardRepository repository;
    private DashboardFactory dashboardFactory;

    @BeforeEach
    void init() {
        User user = new User(nextUUID());
        this.dashboardFactory = DashboardTestFactory.forUser(user);
        this.repository = new JpaDashboardRepository(entityManager, dashboardFactory);
    }

    @Test
    void shouldAddAndGetDashboard() {
        Dashboard expectedDashboard = dashboardFactory.create("dashboard name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedDashboard));
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(expectedDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(expectedDashboard);
        });
    }

    @Test
    void shouldNotGetNotExistingDashboard() {
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(new DashboardId(randomUUID()));
            assertThat(foundDashboard).isEmpty();
        });
    }

    @Test
    void shouldUpdateDashboard() {
        Dashboard expectedDashboard = dashboardFactory.create("old name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedDashboard));
        inTransaction(() -> {
            // TODO extend update with additional fields
            Dashboard dashboard = repository.get(expectedDashboard.id()).get();
            expectedDashboard.delete();
            dashboard.delete();
            repository.save(dashboard);
        });
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(expectedDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(expectedDashboard);
        });
    }
}
