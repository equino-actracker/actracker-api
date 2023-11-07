package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class ActivityRepositoryIntegrationTest extends IntegrationTestBase {

    private EntityManager entityManager = entityManager();
    private JpaActivityRepository repository;

    @BeforeEach
    void setup() {
        this.entityManager = entityManager();
        this.repository = new JpaActivityRepository(entityManager);
    }

    @Test
    void shouldAddAndFindActivity() {
        TenantDto user = newUser().build();
        ActivityDto newActivity = newActivity(user);

        inTransaction(entityManager, () -> {
            repository.add(newActivity);
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });

        inTransaction(entityManager, () -> {
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });
    }

    @Test
    void shouldNotFindNotExistingActivity() {
        inTransaction(entityManager, () -> {
            Optional<ActivityDto> foundActivity = repository.findById(randomUUID());
            assertThat(foundActivity).isEmpty();
        });
    }
}
