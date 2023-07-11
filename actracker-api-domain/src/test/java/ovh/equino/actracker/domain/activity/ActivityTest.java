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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    private static final Instant OLD_START_TIME = Instant.ofEpochMilli(1000);
    private static final Instant OLD_END_TIME = Instant.ofEpochMilli(2000);
    private static final Instant NEW_TIME = Instant.ofEpochMilli(1500);

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;
    @Mock
    private MetricsExistenceVerifier metricsExistenceVerifier;

    @BeforeEach
    void init() {
        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(emptySet());
    }

    @Nested
    @DisplayName("rename")
    class RenameTest {
        private static final String NEW_TITLE = "activity new title";
        private static final String OLD_TITLE = "activity old title";

        @Test
        void shouldChangeTitle() {
            // given
            ActivityDto activityDto = new ActivityDto(OLD_TITLE, null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.rename(NEW_TITLE, CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(NEW_TITLE);
        }

        @Test
        void shouldChangeTitleToNull() {
            // given
            ActivityDto activityDto = new ActivityDto(OLD_TITLE, null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.rename(null, CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(null);
        }

        @Test
        void shouldChangeTitleToBlank() {
            // given
            ActivityDto activityDto = new ActivityDto(OLD_TITLE, null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.rename(" ", CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(" ");
        }
    }

    @Nested
    @DisplayName("updateStartTime")
    class UpdateStartTimeTest {

        @Test
        void shouldUpdateStartTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.start(NEW_TIME, CREATOR);

            // then
            assertThat(activity.startTime()).isEqualTo(NEW_TIME);
        }

        @Test
        void shouldSetStartTimeToEndTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.start(OLD_END_TIME, CREATOR);

            // then
            assertThat(activity.startTime()).isEqualTo(OLD_END_TIME);
        }

        @Test
        void shouldSetStartTimeNull() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.start(null, CREATOR);

            // then
            assertThat(activity.startTime()).isNull();
        }

        @Test
        void shouldFailWhenStartTimeSetAfterEndTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // then
            assertThatThrownBy(() ->
                    activity.start(OLD_END_TIME.plusMillis(1), CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }

    }

    @Nested
    @DisplayName("updateEndTime")
    class UpdateEndTimeTest {

        @Test
        void shouldUpdateEndTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.finish(NEW_TIME, CREATOR);

            // then
            assertThat(activity.endTime()).isEqualTo(NEW_TIME);
        }

        @Test
        void shouldSetEndTimeToStartTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.finish(OLD_START_TIME, CREATOR);

            // then
            assertThat(activity.endTime()).isEqualTo(OLD_START_TIME);
        }

        @Test
        void shouldSetEndTimeNull() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.finish(null, CREATOR);

            // then
            assertThat(activity.endTime()).isNull();
        }

        @Test
        void shouldFailWhenEndTimeSetBeforeStartTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // then
            assertThatThrownBy(() ->
                    activity.finish(OLD_START_TIME.minusMillis(1), CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);

        }

    }

    @Nested
    @DisplayName("updateComment")
    class UpdateCommentTest {

        private static final String NEW_COMMENT = "activity new title";
        private static final String OLD_COMMENT = "activity old title";

        @Test
        void shouldUpdateComment() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.updateComment(NEW_COMMENT, CREATOR);

            // then
            assertThat(activity.comment()).isEqualTo(NEW_COMMENT);
        }

        @Test
        void shouldChangeCommentToNull() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, OLD_COMMENT, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.updateComment(null, CREATOR);

            // then
            assertThat(activity.comment()).isNull();
        }

        @Test
        void shouldChangeCommentToBlank() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, OLD_COMMENT, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.updateComment(" ", CREATOR);

            // then
            assertThat(activity.comment()).isEqualTo(" ");
        }

    }

    @Nested
    @DisplayName("assignTag")
    class AssignTagTest {

        @Test
        void shouldAssignFirstTag() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            TagId newTag = new TagId(randomUUID());

            // when
            activity.assignTag(newTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactly(newTag);
        }

        @Test
        void shouldAssignAnotherTag() {
            // given
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), new TagId(randomUUID()));
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            TagId newTag = new TagId(randomUUID());
            Collection<TagId> tagsAfterAssignment = union(existingTags, singleton(newTag));

            // when
            activity.assignTag(newTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(tagsAfterAssignment);
        }

        @Test
        void shouldNotDuplicateAssignedTag() {
            // given
            TagId duplicatedTag = new TagId(randomUUID());
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), duplicatedTag);
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.assignTag(duplicatedTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(existingTags);
        }

        @Test
        void shouldFailWhenAssigningNonExistingTag() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            TagId notExistingTag = new TagId(randomUUID());

            when(tagsExistenceVerifier.notExisting(any())).thenReturn(Set.of(notExistingTag));

            // then
            assertThatThrownBy(() ->
                    activity.assignTag(notExistingTag, CREATOR)
            )
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
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), new TagId(randomUUID()), tagToRemove);
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            Collection<TagId> tagsAfterRemove = removeAll(existingTags, singleton(tagToRemove));

            // when
            activity.removeTag(tagToRemove, CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(tagsAfterRemove);

        }

        @Test
        void shouldKeepTagsEmptyWhenRemovingFromEmptyTags() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.removeTag(new TagId(randomUUID()), CREATOR);

            // then
            assertThat(activity.tags()).isEmpty();
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingUnassignedTag() {
            // given
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), new TagId(randomUUID()), new TagId(randomUUID()));
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.removeTag(new TagId(randomUUID()), CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(existingTags);
        }

    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        void shouldDeleteActivity() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier, metricsExistenceVerifier);

            // when
            activity.delete(CREATOR);

            // then
            assertThat(activity.deleted()).isTrue();
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
                    metricsExistenceVerifier
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
                    metricsExistenceVerifier
            );
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);

            // when
            activity.setMetricValue(newMetricValue, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, newMetricValue);
        }

        @Test
        void shouldFailWhenMetricNonExistValueNotSet() {
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
                    metricsExistenceVerifier
            );
            MetricValue newMetricValue = new MetricValue(NON_EXISTING_METRIC_ID.id(), TEN);
            when(metricsExistenceVerifier.notExisting(any(), any()))
                    .thenReturn(singleton(NON_EXISTING_METRIC_ID));

            // then
            assertThatThrownBy(() ->
                    activity.setMetricValue(newMetricValue, CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenMetricNonExistValueSet() {
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
                    metricsExistenceVerifier
            );
            MetricValue newMetricValue = new MetricValue(NON_EXISTING_METRIC_ID.id(), TEN);
            when(metricsExistenceVerifier.notExisting(any(), any()))
                    .thenReturn(singleton(NON_EXISTING_METRIC_ID));

            // then
            assertThatThrownBy(() ->
                    activity.setMetricValue(newMetricValue, CREATOR)
            )
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
                    metricsExistenceVerifier
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
                    metricsExistenceVerifier
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
                    metricsExistenceVerifier
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
                    metricsExistenceVerifier
            );

            // when
            activity.unsetMetricValue(NON_EXISTING_METRIC_ID, CREATOR);

            // then
            assertThat(activity.metricValues).containsExactlyInAnyOrder(EXISTING_METRIC_VALUE);
        }
    }
}
