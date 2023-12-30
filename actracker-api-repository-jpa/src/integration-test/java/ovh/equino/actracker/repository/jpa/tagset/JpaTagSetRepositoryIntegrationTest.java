package ovh.equino.actracker.repository.jpa.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tagset.TagSet;
import ovh.equino.actracker.domain.tagset.TagSetFactory;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.tagset.TagSetTestFactory;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;

abstract class JpaTagSetRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaTagSetRepository repository;
    private TagSetFactory tagSetFactory;

    @BeforeEach
    void init() {
        User user = new User(nextUUID());
        this.tagSetFactory = TagSetTestFactory.forUser(user);
        this.repository = new JpaTagSetRepository(entityManager, tagSetFactory);
    }

    @Test
    void shouldAddAndGetTagSet() {
        TagSet expectedTagSet = tagSetFactory.create("tag set name", emptySet());
        inTransaction(() -> repository.add(expectedTagSet));
        inTransaction(() -> {
            Optional<TagSet> foundTagSet = repository.get(expectedTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(expectedTagSet);
        });
    }

    @Test
    void shouldNotGetNotExistingTagSet() {
        inTransaction(() -> {
            Optional<TagSet> foundTagSet = repository.get(new TagSetId(randomUUID()));
            assertThat(foundTagSet).isEmpty();
        });
    }

    @Test
    void shouldUpdateTagSet() {
        TagSet expectedTagSet = tagSetFactory.create("old name", emptySet());
        inTransaction(() -> repository.add(expectedTagSet));
        inTransaction(() -> {
            TagSet tagSet = repository.get(expectedTagSet.id()).get();
            expectedTagSet.delete();
            tagSet.delete();
            repository.save(tagSet);
        });
        inTransaction(() -> {
            Optional<TagSet> foundTagSet = repository.get(expectedTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(expectedTagSet);
        });
    }
}
