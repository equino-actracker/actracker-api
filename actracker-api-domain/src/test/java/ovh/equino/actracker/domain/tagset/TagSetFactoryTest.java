package ovh.equino.actracker.domain.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagSetFactoryTest {

    private static final TagSetId TAG_SET_ID = new TagSetId();
    private static final User CREATOR = new User(randomUUID());
    private static final String TAG_SET_NAME = "tag set name";
    private static final Boolean DELETED = TRUE;

    private final TagSetDataSource tagSetDataSource = null;
    @Mock
    private TagDataSource tagDataSource;

    private TagSetFactory tagSetFactory;

    @BeforeEach
    void init() {
        tagSetFactory = new TagSetFactory(tagSetDataSource, tagDataSource);
    }

    @Test
    void shouldCreateMinimalTagSet() {
        // when
        var tagSet = tagSetFactory.create(CREATOR, TAG_SET_NAME, null);

        // then
        assertThat(tagSet.id()).isNotNull();
        assertThat(tagSet.name()).isEqualTo(TAG_SET_NAME);
        assertThat(tagSet.creator()).isEqualTo(CREATOR);
        assertThat(tagSet.tags()).isEmpty();
        assertThat(tagSet.deleted()).isFalse();
    }

    @Test
    void shouldCreateFullTagSet() {
        // given
        var assignedTag1 = new TagId(randomUUID());
        var assignedTag2 = new TagId(randomUUID());
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(
                        List.of(tagDto(assignedTag1), tagDto(assignedTag2))
                );

        // when
        var tagSet = tagSetFactory.create(CREATOR, TAG_SET_NAME, List.of(assignedTag1, assignedTag2));

        // then
        assertThat(tagSet.id()).isNotNull();
        assertThat(tagSet.name()).isEqualTo(TAG_SET_NAME);
        assertThat(tagSet.creator()).isEqualTo(CREATOR);
        assertThat(tagSet.tags()).containsExactlyInAnyOrder(assignedTag1, assignedTag2);
        assertThat(tagSet.deleted()).isFalse();
    }

    @Test
    void shouldCreateFailWhenTagSetInvalid() {
        // given
        var invalidTagSetName = "";

        // then
        assertThatThrownBy(() -> tagSetFactory.create(CREATOR, invalidTagSetName, null))
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldCreateFailWhenTagNonAccessible() {
        // given
        var nonAccessibleTag = new TagId(randomUUID());
        when(tagDataSource.find(any(Set.class), any(User.class))).thenReturn(emptyList());

        // then
        assertThatThrownBy(() -> tagSetFactory.create(CREATOR, TAG_SET_NAME, List.of(nonAccessibleTag)))
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldReconstituteTagSet() {
        // given
        var assignedTags = List.of(new TagId(), new TagId());

        // when
        var tagSet = tagSetFactory.reconstitute(TAG_SET_ID, CREATOR, TAG_SET_NAME, assignedTags, DELETED);

        // then
        assertThat(tagSet.id()).isEqualTo(TAG_SET_ID);
        assertThat(tagSet.name()).isEqualTo(TAG_SET_NAME);
        assertThat(tagSet.creator()).isEqualTo(CREATOR);
        assertThat(tagSet.tags()).containsExactlyInAnyOrderElementsOf(assignedTags);
        assertThat(tagSet.deleted()).isTrue();
    }

    private TagDto tagDto(TagId tagId) {
        return new TagDto(tagId.id(), randomUUID(), null, null, null, !DELETED);
    }
}
