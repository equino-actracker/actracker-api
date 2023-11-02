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

    @BeforeEach
    void setup() {
        this.entityManager = entityManager();
    }

    @Test
    void shouldAddAndFindActivity() {
        TenantDto user = newUser();
        ActivityDto newActivity = newActivity(user);

        JpaActivityRepository repository = new JpaActivityRepository(entityManager);
        inTransaction(entityManager, () -> {
                    repository.add(newActivity);
                    Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
                    assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
                }
        );

        inTransaction(entityManager, () -> {
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });
    }

    @Test
    void shouldNotFindNotExistingActivity() {
        JpaActivityRepository repository = new JpaActivityRepository(entityManager);
        inTransaction(entityManager, () -> {
            Optional<ActivityDto> foundActivity = repository.findById(randomUUID());
            assertThat(foundActivity).isEmpty();
        });
    }
}
