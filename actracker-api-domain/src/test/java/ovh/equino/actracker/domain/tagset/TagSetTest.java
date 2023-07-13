package ovh.equino.actracker.domain.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.*;

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
class TagSetTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String TAG_SET_NAME = "tag set name";
    private static final List<TagId> EMPTY_TAGS = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;
    @Mock
    private TagSetValidator validator;

    @BeforeEach
    void init() {
        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(emptySet());
    }

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
            assertThatThrownBy(() ->
                    tagSet.rename(NEW_NAME, CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("assignTag")
    class AssignTagTest {

        @Test
        void shouldAssignFirstTag() {
            // given
            TagSetDto tagSetDto = new TagSetDto("tag set name", emptySet());
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            TagId newTag = new TagId(randomUUID());

            // when
            tagSet.assignTag(newTag, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactly(newTag);
        }

        @Test
        void shouldAssignAnotherTag() {
            // given
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), new TagId(randomUUID()));
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            TagSetDto tagSetDto = new TagSetDto("tag set name", existingTagIds);
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            TagId newTag = new TagId(randomUUID());
            Collection<TagId> tagsAfterAssignment = union(existingTags, singleton(newTag));

            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(new HashSet<>(union(existingTags, singleton(newTag))));

            // when
            tagSet.assignTag(newTag, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactlyInAnyOrderElementsOf(tagsAfterAssignment);
        }

        @Test
        void shouldNotDuplicateAssignedTag() {
            // given
            TagId duplicatedTag = new TagId(randomUUID());
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), duplicatedTag);
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            TagSetDto tagSetDto = new TagSetDto("tag set name", existingTagIds);
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);

            // when
            tagSet.assignTag(duplicatedTag, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactlyInAnyOrderElementsOf(existingTags);
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
            assertThatThrownBy(() ->
                    tagSet.assignTag(newTag, CREATOR)
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
            TagSetDto tagSetDto = new TagSetDto("tag set name", existingTagIds);
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            Collection<TagId> tagsAfterRemove = removeAll(existingTags, singleton(tagToRemove));

            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);

            // when
            tagSet.removeTag(tagToRemove, CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactlyInAnyOrderElementsOf(tagsAfterRemove);
        }

        @Test
        void shouldKeepTagsEmptyWhenRemovingFromEmptyTags() {
            // given
            TagSetDto tagSetDto = new TagSetDto("tag set name", emptySet());
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            // when
            tagSet.removeTag(new TagId(randomUUID()), CREATOR);

            // then
            assertThat(tagSet.tags()).isEmpty();
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingUnassignedTag() {
            // given
            Set<TagId> existingTags = Set.of(new TagId(randomUUID()), new TagId(randomUUID()), new TagId(randomUUID()));
            Set<UUID> existingTagIds = existingTags.stream().map(TagId::id).collect(toSet());
            TagSetDto tagSetDto = new TagSetDto("tag set name", existingTagIds);
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            when(tagsExistenceVerifier.existing(any()))
                    .thenReturn(existingTags);

            // when
            tagSet.removeTag(new TagId(randomUUID()), CREATOR);

            // then
            assertThat(tagSet.tags()).containsExactlyInAnyOrderElementsOf(existingTags);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        void shouldDeleteTagSet() {
            // given
            TagSetDto tagSetDto = new TagSetDto("tag set name", emptySet());
            TagSet tagSet = TagSet.create(tagSetDto, CREATOR, tagsExistenceVerifier);

            // when
            tagSet.delete(CREATOR);

            // then
            assertThat(tagSet.deleted()).isTrue();
        }

    }
}
