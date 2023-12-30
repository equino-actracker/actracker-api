package ovh.equino.actracker.repository.jpa.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.Collections.emptyList;
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
        MetricFactory metricFactory = MetricTestFactory.forUser(user);
        this.repository = new JpaTagRepository(entityManager, tagFactory, metricFactory);
    }

    @Test
    void shouldAddAndGetTag() {
        Tag expectedTag = tagFactory.create("tag name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedTag));
        inTransaction(() -> {
            Optional<Tag> foundTag = repository.get(expectedTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(expectedTag);
        });
    }

    @Test
    void shouldNotGetNotExistingTag() {
        inTransaction(() -> {
            Optional<Tag> foundTag = repository.get(new TagId(nextUUID()));
            assertThat(foundTag).isEmpty();
        });
    }

    @Test
    void shouldUpdateTag() {
        Tag expectedTag = tagFactory.create("old name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedTag));
//        inTransaction(() -> {
            Tag tag = repository.get(expectedTag.id()).get();
            expectedTag.delete();
            tag.delete();
            repository.save(tag);
//        });
//        inTransaction(() -> {
            Optional<Tag> foundTag = repository.get(expectedTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(expectedTag);
//        });
    }
}
