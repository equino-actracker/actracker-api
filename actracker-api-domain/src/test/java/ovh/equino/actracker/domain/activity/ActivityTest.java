package ovh.equino.actracker.domain.activity;

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
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.math.BigDecimal.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
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
    private ActorExtractor actorExtractor;
    @Mock
    private ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private MetricsAccessibilityVerifier metricsAccessibilityVerifier;
    @Mock
    private ActivityValidator validator;

    @BeforeEach
    void init() {
        when(actorExtractor.getActor()).thenReturn(CREATOR);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.rename(NEW_TITLE);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.rename(null);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.rename(" ");

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.rename(NEW_TITLE))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.rename(NEW_TITLE))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.rename(NEW_TITLE))
                    .isInstanceOf(EntityEditForbidden.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.start(NEW_START_TIME);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.start(END_TIME);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.start(null);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.start(NEW_START_TIME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.start(NEW_START_TIME))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.start(NEW_START_TIME))
                    .isInstanceOf(EntityEditForbidden.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.finish(NEW_END_TIME);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.finish(START_TIME);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.finish(null);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.finish(NEW_END_TIME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.finish(NEW_END_TIME))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.finish(NEW_END_TIME))
                    .isInstanceOf(EntityEditForbidden.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.updateComment(NEW_COMMENT);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.updateComment(null);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.updateComment(" ");

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.updateComment(NEW_COMMENT))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.updateComment(NEW_COMMENT))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.updateComment(NEW_COMMENT))
                    .isInstanceOf(EntityEditForbidden.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            TagId newTag = new TagId();
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            activity.assignTag(newTag);

            // then
            assertThat(activity.tags()).containsExactly(newTag);
        }

        @Test
        void shouldAssignAnotherTag() {
            // given
            TagId existingTag = new TagId();
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            TagId newTag = new TagId();
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            activity.assignTag(newTag);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrder(existingTag, newTag);
        }

        @Test
        void shouldNotDuplicateAssignedTag() {
            // given
            TagId existingTag = new TagId();
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            activity.assignTag(existingTag);

            // then
            assertThat(activity.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldFailWhenAssigningNonAccessibleTag() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            TagId newTag = new TagId();
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.assignTag(newTag))
                    .isInstanceOf(EntityInvalidException.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.assignTag(new TagId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.assignTag(new TagId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.assignTag(new TagId()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("removeTag")
    class RemoveTagTest {

        @Test
        void shouldRemoveAssignedTag() {
            // given
            TagId tagToRemove = new TagId(randomUUID());
            TagId existingTag = new TagId(randomUUID());
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            activity.removeTag(tagToRemove);

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            activity.removeTag(new TagId());

            // then
            assertThat(activity.tags()).isEmpty();
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingUnassignedTag() {
            // given
            TagId existingTag = new TagId();
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            activity.removeTag(new TagId());

            // then
            assertThat(activity.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingNonAccessibleTag() {
            // given
            TagId tagToRemove = new TagId(randomUUID());
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    Set.of(tagToRemove),
                    EMPTY_METRIC_VALUES,
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // when
            activity.removeTag(tagToRemove);

            // then
            assertThat(activity.tags()).containsExactly(tagToRemove);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.removeTag(new TagId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            TagId existingTag = new TagId();
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.removeTag(existingTag))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagId existingTag = new TagId();
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.removeTag(existingTag))
                    .isInstanceOf(EntityEditForbidden.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.delete();

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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(activity::delete)
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(activity::delete)
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(activity::delete)
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("setMetricValue")
    class SetMetricValue {

        private static final MetricId EXISTING_METRIC_ID = new MetricId();
        private static final MetricId NON_EXISTING_METRIC_ID = new MetricId();
        private static final MetricValue EXISTING_METRIC_VALUE = new MetricValue(EXISTING_METRIC_ID.id(), ZERO);
        private static final MetricValue NON_EXISTING_METRIC_VALUE = new MetricValue(NON_EXISTING_METRIC_ID.id(), ONE);

        @Test
        void shouldSetNewValueIfExistingMetricNotSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_TITLE,
                    EMPTY_TAGS,
                    List.of(NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);
            when(metricsAccessibilityVerifier.isAccessibleFor(any(), any(), any())).thenReturn(true);

            // when
            activity.setMetricValue(newMetricValue);

            // then
            assertThat(activity.metricValues()).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, newMetricValue);
        }

        @Test
        void shouldSetNewValueIfExistingMetricAlreadySet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);
            when(metricsAccessibilityVerifier.isAccessibleFor(any(), any(), any())).thenReturn(true);

            // when
            activity.setMetricValue(newMetricValue);

            // then
            assertThat(activity.metricValues()).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, newMetricValue);
        }

        @Test
        void shouldFailWhenSettingValueOfNonAccessibleMetric() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_TITLE,
                    EMPTY_TAGS,
                    List.of(NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);
            when(metricsAccessibilityVerifier.isAccessibleFor(any(), any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.setMetricValue(newMetricValue))
                    .isInstanceOf(EntityInvalidException.class);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(metricsAccessibilityVerifier.isAccessibleFor(any(), any(), any())).thenReturn(true);
            doThrow(EntityInvalidException.class).when(validator).validate(any());
            MetricValue newMetricValue = new MetricValue(randomUUID(), TEN);

            // then
            assertThatThrownBy(() -> activity.setMetricValue(newMetricValue))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);

            // then
            assertThatThrownBy(() -> activity.setMetricValue(newMetricValue))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            MetricValue newMetricValue = new MetricValue(EXISTING_METRIC_ID.id(), TEN);

            // then
            assertThatThrownBy(() -> activity.setMetricValue(newMetricValue))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("unsetMetricValue")
    class UnsetMetricValue {

        private static final MetricId EXISTING_METRIC_ID = new MetricId();
        private static final MetricId NON_EXISTING_METRIC_ID = new MetricId();
        private static final MetricValue EXISTING_METRIC_VALUE = new MetricValue(EXISTING_METRIC_ID.id(), ZERO);
        private static final MetricValue NON_EXISTING_METRIC_VALUE = new MetricValue(NON_EXISTING_METRIC_ID.id(), ONE);

        @Test
        void shouldRemoveValueWhenExistingMetricValueSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(metricsAccessibilityVerifier.isAccessibleFor(any(), any(), any())).thenReturn(true);

            // when
            activity.unsetMetricValue(EXISTING_METRIC_ID);

            // then
            assertThat(activity.metricValues()).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldLeaveValuesUnchangedWhenExistingMetricValueNotSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(EXISTING_METRIC_ID);

            // then
            assertThat(activity.metricValues()).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldLeaveValuesUnchangedWhenNonAccessibleMetricValueSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(NON_EXISTING_METRIC_ID);

            // then
            assertThat(activity.metricValues()).containsExactlyInAnyOrder(NON_EXISTING_METRIC_VALUE, EXISTING_METRIC_VALUE);
        }

        @Test
        void shouldLeaveValuesUnchangedWhenNonAccessibleMetricValueNotSet() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );

            // when
            activity.unsetMetricValue(NON_EXISTING_METRIC_ID);

            // then
            assertThat(activity.metricValues()).containsExactlyInAnyOrder(EXISTING_METRIC_VALUE);
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
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            when(metricsAccessibilityVerifier.isAccessibleFor(any(), any(), any())).thenReturn(true);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> activity.unsetMetricValue(new MetricId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> activity.unsetMetricValue(EXISTING_METRIC_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Activity activity = new Activity(
                    new ActivityId(),
                    CREATOR,
                    ACTIVITY_TITLE,
                    START_TIME,
                    END_TIME,
                    ACTIVITY_COMMENT,
                    EMPTY_TAGS,
                    List.of(EXISTING_METRIC_VALUE, NON_EXISTING_METRIC_VALUE),
                    !DELETED,
                    actorExtractor,
                    activitiesAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    metricsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(activitiesAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> activity.unsetMetricValue(EXISTING_METRIC_ID))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }
}
