package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.math.BigDecimal.*;
import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.removeAll;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String ACTIVITY_TITLE = "activity title";
    private static final Instant START_TIME = Instant.ofEpochMilli(1000);
    private static final Instant END_TIME = Instant.ofEpochMilli(2000);
    private static final String ACTIVITY_COMMENT = "activity comment";
    private static final List<TagId> EMPTY_TAGS = emptyList();
    private static final List<MetricValue> EMPTY_METRIC_VALUES = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;
    @Mock
    private MetricsExistenceVerifier metricsExistenceVerifier;
    @Mock
    private ActivityValidator validator;

    @BeforeEach
    void init() {
        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(emptySet());
        when(metricsExistenceVerifier.notExisting(any(), any()))
                .thenReturn(emptySet());
    }

    @Nested
    @DisplayName("rename")
    class RenameTest {
        private static final String NEW_TITLE = "activity new title";

        @Test
        void shouldChangeTitle() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.rename(NEW_TITLE, CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(NEW_TITLE);
        }

        @Test
        void shouldChangeTitleToNull() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.rename(null, CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(null);
        }

        @Test
        void shouldChangeTitleToBlank() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.rename(" ", CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(" ");
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.rename(NEW_TITLE, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("updateStartTime")
    class UpdateStartTimeTest {

        private static final Instant NEW_START_TIME = Instant.ofEpochMilli(500);

        @Test
        void shouldUpdateStartTime() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.start(NEW_START_TIME, CREATOR);

            // then
            assertThat(activity.startTime()).isEqualTo(NEW_START_TIME);
        }

        @Test
        void shouldSetStartTimeToEndTime() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.start(END_TIME, CREATOR);

            // then
            assertThat(activity.startTime()).isEqualTo(END_TIME);
        }

        @Test
        void shouldSetStartTimeNull() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.start(null, CREATOR);

            // then
            assertThat(activity.startTime()).isNull();
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.start(NEW_START_TIME, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("updateEndTime")
    class UpdateEndTimeTest {

        private static final Instant NEW_END_TIME = Instant.ofEpochMilli(2500);

        @Test
        void shouldUpdateEndTime() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.finish(NEW_END_TIME, CREATOR);

            // then
            assertThat(activity.endTime()).isEqualTo(NEW_END_TIME);
        }

        @Test
        void shouldSetEndTimeToStartTime() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.finish(START_TIME, CREATOR);

            // then
            assertThat(activity.endTime()).isEqualTo(START_TIME);
        }

        @Test
        void shouldSetEndTimeNull() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.finish(null, CREATOR);

            // then
            assertThat(activity.endTime()).isNull();
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.finish(NEW_END_TIME, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("updateComment")
    class UpdateCommentTest {

        private static final String NEW_COMMENT = "activity new comment";

        @Test
        void shouldUpdateComment() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.updateComment(NEW_COMMENT, CREATOR);

            // then
            assertThat(activity.comment()).isEqualTo(NEW_COMMENT);
        }

        @Test
        void shouldChangeCommentToNull() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.updateComment(null, CREATOR);

            // then
            assertThat(activity.comment()).isNull();
        }

        @Test
        void shouldChangeCommentToBlank() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.updateComment(" ", CREATOR);

            // then
            assertThat(activity.comment()).isEqualTo(" ");
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.updateComment(NEW_COMMENT, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("assignTag")
    class AssignTagTest {

        @Test
        void shouldAssignFirstTag() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            TagId newTag = new TagId();

            // when
            activity.assignTag(newTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactly(newTag);
        }

        @Test
        void shouldAssignAnotherTag() {
            // given
            TagId existingTag = new TagId();
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(singleton(existingTag));
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    singleton(existingTag),
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            TagId newTag = new TagId();

            // when
            activity.assignTag(newTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrder(existingTag, newTag);
        }

        @Test
        void shouldNotDuplicateAssignedTag() {
            // given
            TagId existingTag = new TagId();
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(singleton(existingTag));
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    singleton(existingTag),
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.assignTag(existingTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.assignTag(new TagId(), CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("removeTag")
    class RemoveTagTest {

        @Test
        void
        shouldRemoveAssignedTag() {
            // given
            TagId tagToRemove = new TagId(randomUUID());
            TagId existingTag = new TagId(randomUUID());
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(Set.of(existingTag, tagToRemove));
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    Set.of(existingTag, tagToRemove),
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.removeTag(tagToRemove, CREATOR);

            // then
            assertThat(activity.tags()).containsExactly(existingTag);

        }

        @Test
        void shouldKeepTagsEmptyWhenRemovingFromEmptyTags() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.removeTag(new TagId(), CREATOR);

            // then
            assertThat(activity.tags()).isEmpty();
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingUnassignedTag() {
            // given
            TagId existingTag = new TagId();
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(singleton(existingTag));
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    singleton(existingTag),
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.removeTag(new TagId(), CREATOR);

            // then
            assertThat(activity.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.removeTag(new TagId(), CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        void shouldDeleteActivity() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.delete(CREATOR);

            // then
            assertThat(activity.deleted()).isTrue();
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.delete(CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("setMetricValue")
    class SetMetricValue {

        private static final MetricId EXISTING_METRIC_ID = new MetricId();
        private static final MetricId NON_EXISTING_METRIC_ID = new MetricId();
        private static final MetricValue EXISTING_METRIC_VALUE = new MetricValue(EXISTING_METRIC_ID.id(), ZERO);
        private static final MetricValue NON_EXISTING_METRIC_VALUE = new MetricValue(NON_EXISTING_METRIC_ID.id(), ONE);

        @BeforeEach
        void setUp() {
            when(metricsExistenceVerifier.existing(any(), any())).thenReturn(singleton(EXISTING_METRIC_ID));
            when(metricsExistenceVerifier.notExisting(any(), any()))
                    .thenReturn(singleton(NON_EXISTING_METRIC_ID))
                    .thenReturn(emptySet());
        }

        @Test
        void shouldSetNewValueIfExistingMetricNotSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    "activity title",
                    null,
                    null,
                    null,
                    emptyList(),
                    List.of(NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);

            // when
            activity.setMetricValue(newMetricValue, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, newMetricValue);
        }

        @Test
        void shouldSetNewValueIfExistingMetricAlreadySet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    "activity title",
                    null,
                    null,
                    null,
                    emptyList(),
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);

            // when
            activity.setMetricValue(newMetricValue, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, newMetricValue);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());
            MetricValue newMetricValue = new MetricValue(randomUUID(), TEN);

            // then
            assertThatThrownBy(() -> activity.setMetricValue(newMetricValue, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("unsetMetricValue")
    class UnsetMetricValue {

        private static final MetricId EXISTING_METRIC_ID = new MetricId();
        private static final MetricId NON_EXISTING_METRIC_ID = new MetricId();
        private static final MetricValue EXISTING_METRIC_VALUE = new MetricValue(EXISTING_METRIC_ID.id(), ZERO);
        private static final MetricValue NON_EXISTING_METRIC_VALUE = new MetricValue(NON_EXISTING_METRIC_ID.id(), ONE);

        @BeforeEach
        void setUp() {
            when(metricsExistenceVerifier.existing(any(), any())).thenReturn(singleton(EXISTING_METRIC_ID));
            when(metricsExistenceVerifier.notExisting(any(), any()))
                    .thenReturn(singleton(NON_EXISTING_METRIC_ID))
                    .thenReturn(emptySet());
        }

        @Test
        void shouldRemoveValueWhenExistingMetricValueSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    "activity title",
                    null,
                    null,
                    null,
                    emptyList(),
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(EXISTING_METRIC_ID, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldLeaveValuesUnchangedWhenExistingMetricValueNotSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    "activity title",
                    null,
                    null,
                    null,
                    emptyList(),
                    List.of(NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(EXISTING_METRIC_ID, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldLeaveValuesUnchangedWhenNonExistingMetricValueSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    "activity title",
                    null,
                    null,
                    null,
                    emptyList(),
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(NON_EXISTING_METRIC_ID, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldLeaveValuesUnchangedWhenNonExistingMetricValueNotSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    "activity title",
                    null,
                    null,
                    null,
                    emptyList(),
                    List.of(EXISTING_METRIC_VALUE),
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(NON_EXISTING_METRIC_ID, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    tagsExistenceVerifier,
                    metricsExistenceVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.unsetMetricValue(new MetricId(), CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }
    }
}
