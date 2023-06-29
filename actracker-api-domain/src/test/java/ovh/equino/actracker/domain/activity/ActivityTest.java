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
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.removeAll;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityTest {

    private static final User CREATOR = new User(randomUUID());

    private static final Instant OLD_START_TIME = Instant.ofEpochMilli(1000);
    private static final Instant OLD_END_TIME = Instant.ofEpochMilli(2000);
    private static final Instant NEW_TIME = Instant.ofEpochMilli(1500);

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;

    @BeforeEach
    void init() {
        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(emptySet());
    }

    @Test
    void alwaysFailingTest() {
        fail();
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
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.rename(NEW_TITLE, CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(NEW_TITLE);
        }

        @Test
        void shouldChangeTitleToNull() {
            // given
            ActivityDto activityDto = new ActivityDto(OLD_TITLE, null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.rename(null, CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(null);
        }

        @Test
        void shouldChangeTitleToBlank() {
            // given
            ActivityDto activityDto = new ActivityDto(OLD_TITLE, null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.rename(" ", CREATOR);

            // then
            assertThat(activity.title()).isEqualTo(" ");
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto(OLD_TITLE, null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.rename(NEW_TITLE, unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("updateStartTime")
    class UpdateStartTimeTest {

        @Test
        void shouldUpdateStartTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.start(NEW_TIME, CREATOR);

            // then
            assertThat(activity.startTime()).isEqualTo(NEW_TIME);
        }

        @Test
        void shouldSetStartTimeToEndTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.start(OLD_END_TIME, CREATOR);

            // then
            assertThat(activity.startTime()).isEqualTo(OLD_END_TIME);
        }

        @Test
        void shouldSetStartTimeNull() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.start(null, CREATOR);

            // then
            assertThat(activity.startTime()).isNull();
        }

        @Test
        void shouldFailWhenStartTimeSetAfterEndTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // then
            assertThatThrownBy(() ->
                    activity.start(OLD_END_TIME.plusMillis(1), CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.start(NEW_TIME, unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("updateEndTime")
    class UpdateEndTimeTest {

        @Test
        void shouldUpdateEndTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.finish(NEW_TIME, CREATOR);

            // then
            assertThat(activity.endTime()).isEqualTo(NEW_TIME);
        }

        @Test
        void shouldSetEndTimeToStartTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.finish(OLD_START_TIME, CREATOR);

            // then
            assertThat(activity.endTime()).isEqualTo(OLD_START_TIME);
        }

        @Test
        void shouldSetEndTimeNull() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.finish(null, CREATOR);

            // then
            assertThat(activity.endTime()).isNull();
        }

        @Test
        void shouldFailWhenEndTimeSetBeforeStartTime() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // then
            assertThatThrownBy(() ->
                    activity.finish(OLD_START_TIME.minusMillis(1), CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);

        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto("activity name", OLD_START_TIME, OLD_END_TIME, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.finish(NEW_TIME, unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);

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
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.updateComment(NEW_COMMENT, CREATOR);

            // then
            assertThat(activity.comment()).isEqualTo(NEW_COMMENT);
        }

        @Test
        void shouldChangeCommentToNull() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, OLD_COMMENT, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.updateComment(null, CREATOR);

            // then
            assertThat(activity.comment()).isNull();
        }

        @Test
        void shouldChangeCommentToBlank() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, OLD_COMMENT, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.updateComment(" ", CREATOR);

            // then
            assertThat(activity.comment()).isEqualTo(" ");
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, OLD_COMMENT, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.updateComment(NEW_COMMENT, unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("assignTag")
    class AssignTagTest {

        @Test
        void shouldAssignFirstTag() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

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
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

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
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.assignTag(duplicatedTag, CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(existingTags);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            TagId newTag = new TagId(randomUUID());

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.assignTag(newTag, unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);
        }

        @Test
        void shouldFailWhenAssigningNonExistingTag() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

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
        void shouldRemoveAssignedTag() {
            // given
            TagId tagToRemove = new TagId(randomUUID());
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), new TagId(randomUUID()), tagToRemove);
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

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
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

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
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, existingTagIds, emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.removeTag(new TagId(randomUUID()), CREATOR);

            // then
            assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(existingTags);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.removeTag(new TagId(randomUUID()), unprivilegedUser)
            ).isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        void shouldDeleteActivity() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            // when
            activity.delete(CREATOR);

            // then
            assertThat(activity.deleted()).isTrue();
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            ActivityDto activityDto = new ActivityDto("activity title", null, null, null, emptySet(), emptyList());
            Activity activity = Activity.create(activityDto, CREATOR, tagsExistenceVerifier);

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() ->
                    activity.delete(unprivilegedUser)
            )
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("metricValues")
    class MetricValuesTest {
        @Test
        void implementThem() {
            fail();
        }
    }
}
