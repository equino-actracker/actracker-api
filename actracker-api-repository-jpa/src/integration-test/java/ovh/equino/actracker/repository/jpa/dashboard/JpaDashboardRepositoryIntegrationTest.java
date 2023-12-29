package ovh.equino.actracker.repository.jpa.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardFactory;
import ovh.equino.actracker.domain.dashboard.DashboardTestFactory;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
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
    void shouldAddAndFindDashboard() {
        TenantDto user = newUser().build();
        DashboardDto newDashboard = newDashboard(user).build();

        inTransaction(() -> {
            repository.add(newDashboard);
            Optional<DashboardDto> foundDashboard = repository.findById(newDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(newDashboard);
        });

        inTransaction(() -> {
            Optional<DashboardDto> foundDashboard = repository.findById(newDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(newDashboard);
        });
    }

    @Test
    void shouldNotFindNotExistingDashboard() {
        inTransaction(() -> {
            Optional<DashboardDto> foundDashboard = repository.findById(randomUUID());
            assertThat(foundDashboard).isEmpty();
        });
    }

    @Test
    void shouldAddAndGetDashboard() {
        fail();
    }

    @Test
    void shouldNotGetNotExistingDashboard() {
        fail();
    }

    @Test
    void shouldUpdateDashboard() {
        fail();
    }
}
