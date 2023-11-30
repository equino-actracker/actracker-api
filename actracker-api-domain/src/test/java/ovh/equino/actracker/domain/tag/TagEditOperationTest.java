package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class TagEditOperationTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    private final TagsAccessibilityVerifier tagsAccessibilityVerifier = null;

    @Test
    void shouldPreserveDeletedMetrics() {
        // given
        Metric nonDeletedMetric = new Metric(
                new MetricId(randomUUID()),
                CREATOR,
                "notDeletedMetric",
                MetricType.NUMERIC,
                !DELETED
        );
        Metric deletedMetric = new Metric(
                new MetricId(randomUUID()),
                CREATOR,
                "deletedMetric",
                MetricType.NUMERIC,
                DELETED
        );
        Tag tag = new Tag(
                new TagId(randomUUID()),
                CREATOR,
                "tagName",
                List.of(deletedMetric, nonDeletedMetric),
                emptyList(),
                !DELETED,
                tagsAccessibilityVerifier,
                new TagValidator()
        );
        TagEditOperation editOperation = new TagEditOperation(CREATOR, tag, () -> {
        });

        // when
        editOperation.beforeEditOperation();
        List<Metric> metricsDuringEdit = tag.metrics.stream().toList();
        editOperation.afterEditOperation();

        // then
        assertThat(metricsDuringEdit).extracting(Metric::id)
                .containsExactly(nonDeletedMetric.id());
        assertThat(tag.metrics).extracting(Metric::id)
                .containsExactlyInAnyOrder(deletedMetric.id(), nonDeletedMetric.id());
    }

}