package ovh.equino.actracker.repository.jpa.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class JpaDashboardRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaDashboardRepository repository;

    @BeforeEach
    void setup() {
        this.repository = new JpaDashboardRepository(entityManager);
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
}
