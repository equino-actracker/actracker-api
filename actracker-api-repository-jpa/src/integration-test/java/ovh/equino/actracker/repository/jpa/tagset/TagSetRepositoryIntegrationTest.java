package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class TagSetRepositoryIntegrationTest extends IntegrationTestBase {

    private EntityManager entityManager = entityManager();
    private JpaTagSetRepository repository;

    @BeforeEach
    void setup() {
        this.entityManager = entityManager();
        this.repository = new JpaTagSetRepository(entityManager);
    }

    @Test
    void shouldAddAndFindTagSet() {
        TenantDto user = newUser();
        TagSetDto newTagSet = newTagSet(user);

        inTransaction(entityManager, () -> {
            repository.add(newTagSet);
            Optional<TagSetDto> foundTagSet = repository.findById(newTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(newTagSet);
        });

        inTransaction(entityManager, () -> {
            Optional<TagSetDto> foundTagSet = repository.findById(newTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(newTagSet);
        });
    }

    @Test
    void shouldNotFindNotExistingTagSet() {
        inTransaction(entityManager, () -> {
            Optional<TagSetDto> foundTag = repository.findById(randomUUID());
            assertThat(foundTag).isEmpty();
        });
    }
}
