package ovh.equino.actracker.repository.jpa.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.TagSet;
import ovh.equino.actracker.domain.tagset.TagSetFactory;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.tagset.TagSetTestFactory;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.jpa.TestUtil.nextUUID;

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
    void shouldAddAndGetMinimalTagSet() {
        TagSet expectedTagSet = tagSetFactory.create("tag set name", emptyList());
        inTransaction(() -> repository.add(expectedTagSet));
        inTransaction(() -> {
            Optional<TagSet> foundTagSet = repository.get(expectedTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(expectedTagSet);
        });
    }

    @Test
    void shouldAddAndGetFullTagSet() {
        TagSet expectedTagSet = tagSetFactory.create("tag set name", List.of(new TagId()));
        inTransaction(() -> repository.add(expectedTagSet));
        inTransaction(() -> {
            Optional<TagSet> foundTagSet = repository.get(expectedTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(expectedTagSet);
        });
    }

    @Test
    void shouldAddAndGetMutatedTagSet() {
        TagId tagToRemove = new TagId();
        TagSet expectedTagSet = tagSetFactory.create("old tag set name", List.of(tagToRemove));

        expectedTagSet.rename("new tag set name");
        expectedTagSet.removeTag(tagToRemove);
        expectedTagSet.assignTag(new TagId());
        expectedTagSet.delete();

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
    void shouldUpdateTagSet() throws SQLException {
        TenantDto user = newUser().build();
        TagDto tagToRemove = newTag(user).build();
        TagDto tagToAdd = newTag(user).build();
        database().addTags(tagToRemove, tagToAdd);

        TagSet expectedTagSet = tagSetFactory.create("old name", List.of(new TagId(tagToRemove.id())));

        inTransaction(() -> repository.add(expectedTagSet));

        inTransaction(() -> {
            TagSet tagSet = repository.get(expectedTagSet.id()).get();

            expectedTagSet.rename("new tag set name");
            expectedTagSet.removeTag(new TagId(tagToRemove.id()));
            expectedTagSet.assignTag(new TagId(tagToAdd.id()));
            expectedTagSet.delete();

            tagSet.rename("new tag set name");
            tagSet.removeTag(new TagId(tagToRemove.id()));
            tagSet.assignTag(new TagId(tagToAdd.id()));
            tagSet.delete();

            repository.save(tagSet);
        });

        inTransaction(() -> {
            Optional<TagSet> foundTagSet = repository.get(expectedTagSet.id());
            assertThat(foundTagSet).get().usingRecursiveComparison().isEqualTo(expectedTagSet);
        });
    }
}
