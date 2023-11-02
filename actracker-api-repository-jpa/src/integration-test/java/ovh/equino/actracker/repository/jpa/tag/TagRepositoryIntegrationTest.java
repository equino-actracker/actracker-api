package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class TagRepositoryIntegrationTest extends IntegrationTestBase {

    private EntityManager entityManager = entityManager();
    private JpaTagRepository repository;

    @BeforeEach
    void setup() {
        this.entityManager = entityManager();
        this.repository = new JpaTagRepository(entityManager);
    }

    @Test
    void shouldAddAndFindTag() {
        TenantDto user = newUser();
        TagDto newTag = newTag(user);

        inTransaction(entityManager, () -> {
            repository.add(newTag);
            Optional<TagDto> foundTag = repository.findById(newTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(newTag);
        });

        inTransaction(entityManager, () -> {
            Optional<TagDto> foundTag = repository.findById(newTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(newTag);
        });
    }

    @Test
    void shouldNotFindNotExistingTag() {
        inTransaction(entityManager, () -> {
            Optional<TagDto> foundTag = repository.findById(randomUUID());
            assertThat(foundTag).isEmpty();
        });
    }
}
