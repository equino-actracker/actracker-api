package ovh.equino.actracker.repository.jpa.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;

abstract class JpaTagRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaTagRepository repository;
    private User user;
    private TagFactory tagFactory;
    private MetricFactory metricFactory;

    @BeforeEach
    void init() {
        this.user = new User(nextUUID());
        this.tagFactory = TagTestFactory.forUser(user);
        this.metricFactory = MetricTestFactory.forUser(user);
        this.repository = new JpaTagRepository(entityManager, tagFactory, metricFactory);
    }

    @Test
    void shouldAddAndGetMinimalTag() {
        Tag expectedTag = tagFactory.create("tag name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedTag));
        inTransaction(() -> {
            Optional<Tag> foundTag = repository.get(expectedTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(expectedTag);
        });
    }

    @Test
    void shouldAddAndGetFullTag() {
        Metric metric = metricFactory.create(user, "metric name", NUMERIC);
        Share share = new Share("grantee");
        Tag expectedTag = tagFactory.create("tag name", List.of(metric), List.of(share));
        inTransaction(() -> repository.add(expectedTag));
        inTransaction(() -> {
            Optional<Tag> foundTag = repository.get(expectedTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(expectedTag);
        });
    }

    @Test
    void shouldAddAndGetMutatedTag() {
        Metric metricToDelete = metricFactory.create(user, "metric to delete", NUMERIC);
        Metric metricToRename = metricFactory.create(user, "metric to rename", NUMERIC);
        Share notResolvedShareToDelete = new Share("not resolved share to delete");
        Share resolvedShareToDelete = new Share(new User(nextUUID()), "resolved share to delete");

        Share newNotResolvedShare = new Share("new not resolved share");
        Share newResolvedShare = new Share(new User(nextUUID()), "new resolved share");

        Tag expectedTag = tagFactory.create(
                "old tag name",
                List.of(metricToDelete, metricToRename),
                List.of(notResolvedShareToDelete, resolvedShareToDelete)
        );

        expectedTag.rename("new tag name");
        expectedTag.addMetric("new metric", NUMERIC);
        expectedTag.renameMetric("renamed metric", metricToRename.id());
        expectedTag.deleteMetric(metricToDelete.id());
        expectedTag.unshare(notResolvedShareToDelete.granteeName());
        expectedTag.unshare(resolvedShareToDelete.granteeName());
        expectedTag.share(newNotResolvedShare);
        expectedTag.share(newResolvedShare);
        expectedTag.delete();

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
        inTransaction(() -> {
            // TODO extend update with additional fields
            Tag tag = repository.get(expectedTag.id()).get();
            expectedTag.delete();
            tag.delete();
            repository.save(tag);
        });
        inTransaction(() -> {
            Optional<Tag> foundTag = repository.get(expectedTag.id());
            assertThat(foundTag).get().usingRecursiveComparison().isEqualTo(expectedTag);
        });
    }
}
