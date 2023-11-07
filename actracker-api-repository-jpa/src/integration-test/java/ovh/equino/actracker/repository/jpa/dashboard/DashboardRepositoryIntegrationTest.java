package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class DashboardRepositoryIntegrationTest extends IntegrationTestBase {

    private EntityManager entityManager = entityManager();
    private JpaDashboardRepository repository;

    @BeforeEach
    void setup() {
        this.entityManager = entityManager();
        this.repository = new JpaDashboardRepository(entityManager);
    }

    @Test
    void shouldAddAndFindDashboard() {
        TenantDto user = newUser().build();
        DashboardDto newDashboard = newDashboard(user);

        inTransaction(entityManager, () -> {
            repository.add(newDashboard);
            Optional<DashboardDto> foundDashboard = repository.findById(newDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(newDashboard);
        });

        inTransaction(entityManager, () -> {
            Optional<DashboardDto> foundDashboard = repository.findById(newDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(newDashboard);
        });
    }

    @Test
    void shouldNotFindNotExistingDashboard() {
        inTransaction(entityManager, () -> {
            Optional<DashboardDto> foundDashboard = repository.findById(randomUUID());
            assertThat(foundDashboard).isEmpty();
        });
    }
}
