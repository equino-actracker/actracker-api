package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

@ExtendWith(MockitoExtension.class)
class TagTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String TAG_NAME = "tag name";
    private static final List<Metric> EMPTY_METRICS = emptyList();
    private static final List<Share> EMPTY_SHARES = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private ActorExtractor actorExtractor;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private TagValidator validator;

    @BeforeEach
    void init() {
        when(actorExtractor.getActor()).thenReturn(CREATOR);
    }

    @Nested
    @DisplayName("rename")
    class RenameTagTest {
        private static final String NEW_NAME = "tag new name";

        @Test
        void shouldChangeName() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.rename(NEW_NAME);

            // then
            assertThat(tag.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tag.rename(NEW_NAME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tag.rename(NEW_NAME))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> tag.rename(NEW_NAME))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("addMetric")
    class AddMetricTest {

        private static final String METRIC_NAME = "metric name";

        @Test
        void shouldAddFirstMetric() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            Metric newMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, !DELETED);

            // when
            tag.addMetric(newMetric.name(), newMetric.type());

            // then
            assertThat(tag.metrics())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactly(newMetric);
        }

        @Test
        void shouldAddAnotherMetric() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singleton(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            Metric newMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 2, NUMERIC, !DELETED);

            // when
            tag.addMetric(newMetric.name(), newMetric.type());

            // then
            assertThat(tag.metrics())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrder(existingMetric, newMetric);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Metric newMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tag.addMetric(newMetric.name(), newMetric.type()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNonAccessibleToUser() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);
            Metric newMetric = new Metric(new MetricId(), unauthorizedUser, METRIC_NAME, NUMERIC, !DELETED);

            // then
            assertThatThrownBy(() -> tag.addMetric(newMetric.name(), newMetric.type()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            Metric newMetric = new Metric(new MetricId(), unauthorizedUser, METRIC_NAME, NUMERIC, !DELETED);

            // then
            assertThatThrownBy(() -> tag.addMetric(newMetric.name(), newMetric.type()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("deleteMetric")
    class DeleteMetricTest {

        private static final String METRIC_NAME = "metric name";

        @Test
        void shouldDeleteExistingMetric() {
            // given
            Metric existingMetric1 = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, !DELETED);
            Metric existingMetric2 = new Metric(new MetricId(), CREATOR, METRIC_NAME + 2, NUMERIC, !DELETED);
            Metric metricToDelete = new Metric(new MetricId(), CREATOR, METRIC_NAME + 3, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    List.of(existingMetric1, existingMetric2, metricToDelete),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.deleteMetric(metricToDelete.id());

            // then
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::deleted)
                    .containsExactlyInAnyOrder(
                            tuple(existingMetric1.id(), !DELETED),
                            tuple(existingMetric2.id(), !DELETED),
                            tuple(metricToDelete.id(), DELETED)
                    );
        }

        @Test
        void shouldKeepMetricsEmptyWhenRemovingFromEmptyMetrics() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.deleteMetric(new MetricId());

            // then
            assertThat(tag.metrics()).isEmpty();
        }

        @Test
        void shouldKeepMetricsUnchangedWhenDeletingNotExistingMetrics() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singleton(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.deleteMetric(new MetricId());

            // then
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::deleted)
                    .containsExactlyInAnyOrder(
                            tuple(existingMetric.id(), !DELETED)
                    );
        }

        @Test
        void shouldKeepMetricsUnchangedWhenDeletingAlreadyDeletedMetric() {
            // given
            Metric deletedMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, DELETED);
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 2, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    List.of(deletedMetric, existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.deleteMetric(deletedMetric.id());

            // then
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::deleted)
                    .containsExactlyInAnyOrder(
                            tuple(existingMetric.id(), !DELETED),
                            tuple(deletedMetric.id(), DELETED)
                    );
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tag.deleteMetric(new MetricId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNonAccessibleToUser() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singletonList(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tag.deleteMetric(existingMetric.id()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singletonList(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> tag.deleteMetric(existingMetric.id()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("renameMetric")
    class RenameMetricTest {

        private static final String METRIC_NAME = "metric name";
        private static final String NEW_METRIC_NAME = "new metric name";

        @Test
        void shouldRenameExistingMetric() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, !DELETED);
            Metric metricToRename = new Metric(new MetricId(), CREATOR, METRIC_NAME + 2, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    List.of(existingMetric, metricToRename),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.renameMetric(NEW_METRIC_NAME, metricToRename.id());

            // then
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::name)
                    .containsExactlyInAnyOrder(
                            tuple(existingMetric.id(), existingMetric.name()),
                            tuple(metricToRename.id(), NEW_METRIC_NAME)
                    );
        }

        @Test
        void shouldKeepMetricsUnchangedWhenRenamingNotExistingMetric() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singleton(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.renameMetric(NEW_METRIC_NAME, new MetricId());

            // then
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::name)
                    .containsExactlyInAnyOrder(
                            tuple(existingMetric.id(), existingMetric.name())
                    );
        }

        @Test
        void shouldKeepMetricsUnchangedWhenRenamingDeletedMetric() {
            // given
            Metric nonDeletedMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, !DELETED);
            Metric deletedMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME + 2, NUMERIC, DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    List.of(nonDeletedMetric, deletedMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.renameMetric(NEW_METRIC_NAME, deletedMetric.id());

            // then
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::name)
                    .containsExactlyInAnyOrder(
                            tuple(nonDeletedMetric.id(), nonDeletedMetric.name()),
                            tuple(deletedMetric.id(), deletedMetric.name())
                    );
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tag.renameMetric(NEW_METRIC_NAME, new MetricId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNonAccessibleToUser() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singletonList(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tag.renameMetric(NEW_METRIC_NAME, existingMetric.id()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Metric existingMetric = new Metric(new MetricId(), CREATOR, METRIC_NAME, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    singletonList(existingMetric),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> tag.renameMetric(NEW_METRIC_NAME, existingMetric.id()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTagTest {

        private static final String METRIC_NAME = "metric name";

        @Test
        void shouldDeleteTagAndAllItsMetrics() {
            // given
            Metric metric1 = new Metric(new MetricId(), CREATOR, METRIC_NAME + 1, NUMERIC, !DELETED);
            Metric metric2 = new Metric(new MetricId(), CREATOR, METRIC_NAME + 2, NUMERIC, !DELETED);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    List.of(metric1, metric2),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.delete();

            // then
            assertThat(tag.deleted()).isTrue();
            assertThat(tag.metrics())
                    .extracting(Metric::id, Metric::deleted)
                    .containsExactlyInAnyOrder(
                            tuple(metric1.id(), DELETED),
                            tuple(metric2.id(), DELETED)
                    );
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(tag::delete)
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNonAccessibleToUser() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(tag::delete)
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(tag::delete)
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("share")
    class ShareTagTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldAddNewShareWithoutId() {
            // given
            Share newShare = new Share(GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.share(newShare);

            // then
            assertThat(tag.shares()).containsExactly(newShare);
        }

        @Test
        void shouldAddNewShareWithId() {
            // given
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(randomUUID()),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.share(newShare);

            // then
            assertThat(tag.shares()).containsExactly(newShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithIdAlreadyExists() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.share(newShare);

            // then
            assertThat(tag.shares()).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithoutIdAlreadyExists() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.share(newShare);

            // then
            assertThat(tag.shares()).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithIdAlreadyExists() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Share newShare = new Share(GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.share(newShare);

            // then
            assertThat(tag.shares()).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithoutIdAlreadyExists() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Share newShare = new Share(GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.share(newShare);

            // then
            assertThat(tag.shares()).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tag.share(newShare))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNonAccessibleToUser() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);
            Share newShare = new Share(GRANTEE_NAME);

            // then
            assertThatThrownBy(() -> tag.share(newShare))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            Share newShare = new Share(GRANTEE_NAME);

            // then
            assertThatThrownBy(() -> tag.share(newShare))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("unshare")
    class UnshareTagTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldUnshareTagWhenSharedWithId() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.unshare(existingShare.granteeName());

            // then
            assertThat(tag.shares()).isEmpty();
        }

        @Test
        void shouldUnshareTagWhenSharedWithoutId() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.unshare(existingShare.granteeName());

            // then
            assertThat(tag.shares()).isEmpty();
        }

        @Test
        void shouldLeaveSharesUnchangedWhenNotShared() {
            // given
            Share share1 = new Share(GRANTEE_NAME + 1);
            Share share2 = new Share(new User(randomUUID()), GRANTEE_NAME + 2);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    List.of(share1, share2),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tag.unshare(GRANTEE_NAME);

            // then
            assertThat(tag.shares()).containsExactlyInAnyOrder(share1, share2);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tag.unshare(GRANTEE_NAME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNonAccessibleToUser() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tag.unshare(existingShare.granteeName()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Tag tag = new Tag(
                    new TagId(),
                    CREATOR,
                    TAG_NAME,
                    EMPTY_METRICS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> tag.unshare(existingShare.granteeName()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }
}
