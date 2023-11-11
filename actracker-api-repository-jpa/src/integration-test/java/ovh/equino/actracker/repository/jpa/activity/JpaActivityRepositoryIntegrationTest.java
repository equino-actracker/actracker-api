package ovh.equino.actracker.repository.jpa.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

abstract class JpaActivityRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaActivityRepository repository;

    @BeforeEach
    void init() {
        this.repository = new JpaActivityRepository(entityManager);
    }

    @Test
    void shouldAddAndFindActivity() {
        TenantDto user = newUser().build();
        ActivityDto newActivity = newActivity(user).build();

        inTransaction(() -> {
            repository.add(newActivity);
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });

        inTransaction(() -> {
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });
    }

    @Test
    void shouldNotFindNotExistingActivity() {
        inTransaction(() -> {
            Optional<ActivityDto> foundActivity = repository.findById(randomUUID());
            assertThat(foundActivity).isEmpty();
        });
    }
}
