package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.assertj.core.api.Assertions.*;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

class TagTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    @Nested
    @DisplayName("rename")
    class RenameTest {
        private static final String NEW_NAME = "tag new name";
        private static final String OLD_NAME = "tag old name";

        @Test
        void shouldChangeName() {
            // given
            TagDto tagDto = new TagDto(OLD_NAME, null);
            Tag tag = Tag.create(tagDto, CREATOR);

            // when
            tag.rename(NEW_NAME, CREATOR);

            // then
            assertThat(tag.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenNameNull() {
            // given
            TagDto tagDto = new TagDto(OLD_NAME, null);
            Tag tag = Tag.create(tagDto, CREATOR);

            // then
            assertThatThrownBy(() ->
                    tag.rename(null, CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNameBlank() {
            // given
            TagDto tagDto = new TagDto(OLD_NAME, null);
            Tag tag = Tag.create(tagDto, CREATOR);

            // then
            assertThatThrownBy(() ->
                    tag.rename(" ", CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("addMetric")
    class AddMetricTest {

        @Test
        void shouldAddFirstMetric() {
            // given
            TagDto tagDto = new TagDto("tag name", emptyList());
            Tag tag = Tag.create(tagDto, CREATOR);

            MetricDto newMetricDto = new MetricDto(randomUUID(), "gross", NUMERIC);
            Metric newMetric = Metric.create(newMetricDto, CREATOR);

            // when
            tag.addMetric(newMetricDto.name(), newMetricDto.type(), CREATOR);

            // then
            assertThat(tag.metrics)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactly(newMetric);
        }

        @Test
        void shouldAddAnotherMetric() {
            // given
            List<MetricDto> existingMetrics = List.of(
                    new MetricDto(randomUUID(), "weight", NUMERIC),
                    new MetricDto(randomUUID(), "height", NUMERIC)
            );
            TagDto tagDto = new TagDto("tag name", existingMetrics);
            Tag tag = Tag.create(tagDto, CREATOR);

            MetricDto newMetric = new MetricDto(randomUUID(), "gross", NUMERIC);
            List<Metric> metricsAfterAdd = union(existingMetrics, singletonList(newMetric)).stream()
                    .map(metricDto -> Metric.create(metricDto, CREATOR))
                    .toList();

            // when
            tag.addMetric(newMetric.name(), newMetric.type(), CREATOR);

            // then
            assertThat(tag.metrics)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(metricsAfterAdd);
        }
    }

    @Nested
    @DisplayName("deleteMetric")
    class DeleteMetricTest {


        @Test
        void shouldDeleteExistingMetric() {
            // given
            MetricId metricToDeleteId = new MetricId();
            MetricId existingMetric1Id = new MetricId();
            MetricId existingMetric2Id = new MetricId();
            MetricDto metricToDelete = new MetricDto(metricToDeleteId.id(), CREATOR.id(), "metric to delete", NUMERIC, !DELETED);
            MetricDto existingMetric1 = new MetricDto(existingMetric1Id.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto existingMetric2 = new MetricDto(existingMetric2Id.id(), CREATOR.id(), "metric 2", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(
                    existingMetric1,
                    existingMetric2,
                    metricToDelete
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.deleteMetric(metricToDeleteId, CREATOR);

            // then
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::isDeleted)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(existingMetric1Id, !DELETED),
                            tuple(existingMetric2Id, !DELETED),
                            tuple(metricToDeleteId, DELETED)
                    ));
        }

        @Test
        void shouldKeepMetricsEmptyWhenRemovingFromEmptyMetrics() {
            // given
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", emptyList(), emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.deleteMetric(new MetricId(), CREATOR);

            // then
            assertThat(tag.metrics).isEmpty();
        }

        @Test
        void shouldKeepMetricsUnchangedWhenDeletingNotExistingMetrics() {
            // given
            MetricId existingMetric1Id = new MetricId();
            MetricId existingMetric2Id = new MetricId();
            MetricDto existingMetric1 = new MetricDto(existingMetric1Id.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto existingMetric2 = new MetricDto(existingMetric2Id.id(), CREATOR.id(), "metric 2", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(
                    existingMetric1,
                    existingMetric2
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.deleteMetric(new MetricId(), CREATOR);

            // then
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::isDeleted)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(existingMetric1Id, !DELETED),
                            tuple(existingMetric2Id, !DELETED)
                    ));
        }

        @Test
        void shouldKeepMetricsUnchangedWhenDeletingAlreadyDeletedMetric() {
            // given
            MetricId deletedMetricId = new MetricId();
            MetricId existingMetric1Id = new MetricId();
            MetricId existingMetric2Id = new MetricId();
            MetricDto deletedMetric = new MetricDto(deletedMetricId.id(), CREATOR.id(), "metric to delete", NUMERIC, DELETED);
            MetricDto existingMetric1 = new MetricDto(existingMetric1Id.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto existingMetric2 = new MetricDto(existingMetric2Id.id(), CREATOR.id(), "metric 2", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(
                    existingMetric1,
                    existingMetric2,
                    deletedMetric
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.deleteMetric(deletedMetricId, CREATOR);

            // then
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::isDeleted)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(existingMetric1Id, !DELETED),
                            tuple(existingMetric2Id, !DELETED),
                            tuple(deletedMetricId, DELETED)
                    ));
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            MetricId existingMetricId = new MetricId();
            MetricDto existingMetric1 = new MetricDto(existingMetricId.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(existingMetric1);
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    tag.deleteMetric(existingMetricId, unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("renameMetric")
    class RenameMetricTest {

        @Test
        void shouldRenameExistingMetric() {
            // given
            final String newName = "new metric name";
            MetricId metricToRenameId = new MetricId();
            MetricId existingMetric1Id = new MetricId();
            MetricId existingMetric2Id = new MetricId();
            MetricDto metricToRename = new MetricDto(metricToRenameId.id(), CREATOR.id(), "metric to rename", NUMERIC, !DELETED);
            MetricDto existingMetric1 = new MetricDto(existingMetric1Id.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto existingMetric2 = new MetricDto(existingMetric2Id.id(), CREATOR.id(), "metric 2", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(
                    existingMetric1,
                    existingMetric2,
                    metricToRename
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.renameMetric(newName, metricToRenameId, CREATOR);

            // then
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::name)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(existingMetric1Id, existingMetric1.name()),
                            tuple(existingMetric2Id, existingMetric2.name()),
                            tuple(metricToRenameId, newName)
                    ));
        }

        @Test
        void shouldKeepMetricsUnchangedWhenRenamingNotExistingMetric() {
            // given
            MetricId existingMetric1Id = new MetricId();
            MetricId existingMetric2Id = new MetricId();
            MetricDto existingMetric1 = new MetricDto(existingMetric1Id.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto existingMetric2 = new MetricDto(existingMetric2Id.id(), CREATOR.id(), "metric 2", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(
                    existingMetric1,
                    existingMetric2
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.renameMetric("new metric name", new MetricId(), CREATOR);

            // then
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::name)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(existingMetric1Id, existingMetric1.name()),
                            tuple(existingMetric2Id, existingMetric2.name())
                    ));
        }

        @Test
        void shouldKeepMetricsUnchangedWhenRenamingDeletedMetric() {
            // given
            MetricId notDeletedMetricId = new MetricId();
            MetricId deletedMetricId = new MetricId();
            MetricDto notDeletedMetric = new MetricDto(notDeletedMetricId.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto deletedMetric = new MetricDto(deletedMetricId.id(), CREATOR.id(), "metric 2", NUMERIC, DELETED);
            List<MetricDto> existingMetrics = List.of(
                    notDeletedMetric,
                    deletedMetric
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.renameMetric("new metric name", deletedMetricId, CREATOR);

            // then
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::name)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(notDeletedMetricId, notDeletedMetric.name()),
                            tuple(deletedMetricId, deletedMetric.name())
                    ));
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTagTest {

        @Test
        void shouldDeleteTagAndAllItsMetrics() {
            // given
            MetricId existingMetric1Id = new MetricId();
            MetricId existingMetric2Id = new MetricId();
            MetricDto existingMetric1 = new MetricDto(existingMetric1Id.id(), CREATOR.id(), "metric 1", NUMERIC, !DELETED);
            MetricDto existingMetric2 = new MetricDto(existingMetric2Id.id(), CREATOR.id(), "metric 2", NUMERIC, !DELETED);
            List<MetricDto> existingMetrics = List.of(
                    existingMetric1,
                    existingMetric2
            );
            TagDto tagDto = new TagDto(randomUUID(), CREATOR.id(), "tag name", existingMetrics, emptyList(), !DELETED);
            Tag tag = Tag.fromStorage(tagDto);

            // when
            tag.delete(CREATOR);

            // then
            assertThat(tag.isDeleted()).isTrue();
            assertThat(tag.metrics)
                    .extracting(Metric::id, Metric::isDeleted)
                    .containsExactlyInAnyOrderElementsOf(List.of(
                            tuple(existingMetric1Id, DELETED),
                            tuple(existingMetric2Id, DELETED)
                    ));
        }
    }
}
