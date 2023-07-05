package ovh.equino.actracker.domain.tagset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagSetEditOperationTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;

    @Test
    void shouldPreserveNotExistingTags() {
        // given
        TagId notExistingTag = new TagId(randomUUID());
        TagId existingTag = new TagId(randomUUID());
        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(singleton(notExistingTag));
        when(tagsExistenceVerifier.existing(any()))
                .thenReturn(singleton(existingTag));
        TagSet tagSet = new TagSet(
                new TagSetId(randomUUID()),
                CREATOR,
                "tag set name",
                List.of(notExistingTag, existingTag),
                !DELETED,
                tagsExistenceVerifier
        );
        TagSetEditOperation editOperation = new TagSetEditOperation(CREATOR, tagSet, tagsExistenceVerifier, () -> {
        });

        // when
        editOperation.beforeEditOperation();
        List<TagId> tagsDuringEdit = tagSet.tags.stream().toList();
        editOperation.afterEditOperation();

        // then
        assertThat(tagsDuringEdit).containsExactly(existingTag);
        assertThat(tagSet.tags).containsExactlyInAnyOrder(notExistingTag, existingTag);
    }
}