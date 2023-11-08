package ovh.equino.actracker.repository.jpa.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class TagSetRepositoryIntegrationTest extends IntegrationTestBase {

    private JpaTagSetRepository repository;

    @BeforeEach
    void setup() {
        this.repository = new JpaTagSetRepository(entityManager);
    }

    @Test
    void shouldAddAndFindTagSet() {
        TenantDto user = newUser().build();
        TagSetDto newTagSet = newTagSet(user).build();

        inTransaction(() -> {
            repository.add(newTagSet);
            Optional<TagSetDto> foundTagSet = repository.findById(newTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(newTagSet);
        });

        inTransaction(() -> {
            Optional<TagSetDto> foundTagSet = repository.findById(newTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(newTagSet);
        });
    }

    @Test
    void shouldNotFindNotExistingTagSet() {
        inTransaction(() -> {
            Optional<TagSetDto> foundTag = repository.findById(randomUUID());
            assertThat(foundTag).isEmpty();
        });
    }
}
