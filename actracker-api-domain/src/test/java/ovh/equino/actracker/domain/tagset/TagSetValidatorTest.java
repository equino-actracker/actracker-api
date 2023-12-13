package ovh.equino.actracker.domain.tagset;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagSetValidatorTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    private static final String VALIDATION_ERROR = "TagSet invalid: %s";
    private static final String EMPTY_NAME_ERROR = "Name is empty";

    @Mock
    private TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;

    private final TagSetValidator validator = new TagSetValidator();

    @Test
    void shouldNotFailWhenTagSetValid() {
        // given
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                "tag set name",
                singletonList(new TagId()),
                !DELETED,
                validator,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier
        );

        // then
        assertThatCode(() -> validator.validate(tagSet)).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenNameBlank() {
        // given
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                "   ",
                singletonList(new TagId()),
                !DELETED,
                validator,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(tagSet))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenNameNull() {
        // given
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                null,
                singletonList(new TagId()),
                !DELETED,
                validator,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(tagSet))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }
}