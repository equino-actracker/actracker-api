package ovh.equino.actracker.repository.jpa.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagFactory;
import ovh.equino.actracker.domain.tag.TagTestFactory;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;

abstract class JpaTagRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaTagRepository repository;
    private TagFactory tagFactory;

    @BeforeEach
    void init() {
        User user = new User(nextUUID());
        this.tagFactory = TagTestFactory.forUser(user);
        this.repository = new JpaTagRepository(entityManager, tagFactory);
    }

    @Test
    void shouldAddAndFindTag() {
        TenantDto user = newUser().build();
        TagDto newTag = newTag(user).build();

        inTransaction(() -> {
            repository.add(newTag);
            Optional<TagDto> foundTag = repository.findById(newTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(newTag);
        });

        inTransaction(() -> {
            Optional<TagDto> foundTag = repository.findById(newTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(newTag);
        });
    }

    @Test
    void shouldNotFindNotExistingTag() {
        inTransaction(() -> {
            Optional<TagDto> foundTag = repository.findById(randomUUID());
            assertThat(foundTag).isEmpty();
        });
    }
}
