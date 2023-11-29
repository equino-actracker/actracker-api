package ovh.equino.actracker.domain.tagset;

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

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagSetTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String TAG_SET_NAME = "tag set name";
    private static final List<TagId> EMPTY_TAGS = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;
    @Mock
    private TagSetValidator validator;

    // TODO all should fail when tag set inaccessible to user (entity not found)

    @Nested
    @DisplayName("rename")
    class RenameTest {

        private static final String NEW_NAME = "tag set new name";

        @Test
        void shouldChangeName() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            // when
            tagSet.rename(NEW_NAME, CREATOR);

            // then
            assertThat(tagSet.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.rename(NEW_NAME, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() -> tagSet.rename(NEW_NAME, unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("assignTag")
    class AssignTagTest {

        @Test
        void shouldAssignFirstTag() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            TagId newTag = new TagId(randomUUID());

            // when
            tagSet.assignTag(newTag, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactly(newTag);
        }

        @Test
        void shouldAssignAnotherTag() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(singleton(existingTag));
            TagId newTag = new TagId();

            // when
            tagSet.assignTag(newTag, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactlyInAnyOrder(existingTag, newTag);
        }

        @Test
        void shouldNotDuplicateAssignedTag() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(singleton(existingTag));

            // when
            tagSet.assignTag(existingTag, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactly(existingTag);
        }

        void shouldFailWhenAssigningNonAccessibleTag() {
            // TODO
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            TagId newTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.assignTag(newTag, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            TagId newTag = new TagId();

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() -> tagSet.assignTag(newTag, unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("removeTag")
    class RemoveTagTest {

        @Test
        void shouldRemoveAssignedTag() {
            // given
            TagId tagToPreserve = new TagId();
            TagId tagToRemove = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    List.of(tagToPreserve, tagToRemove),
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(Set.of(tagToPreserve, tagToRemove));

            // when
            tagSet.removeTag(tagToRemove, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactly(tagToPreserve);
        }

        @Test
        void shouldKeepTagsEmptyWhenRemovingFromEmptyTags() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            // when
            tagSet.removeTag(new TagId(), CREATOR);

            // then
            assertThat(tagSet.tags()).isEmpty();
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingUnassignedTag() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(singleton(existingTag));

            // when
            tagSet.removeTag(new TagId(), CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldFailWhenTagSetInvalid() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.removeTag(new TagId(), CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() -> tagSet.removeTag(new TagId(randomUUID()), unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        void shouldDeleteTagSet() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            // when
            tagSet.delete(CREATOR);

            // then
            assertThat(tagSet.deleted()).isTrue();
        }

        @Test
        void shouldFailWhenTagSetInvalid() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.delete(CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    validator,
                    tagsExistenceVerifier
            );

            User unprivilegedUser = new User(randomUUID());

            // then
            assertThatThrownBy(() -> tagSet.delete(unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }
}
