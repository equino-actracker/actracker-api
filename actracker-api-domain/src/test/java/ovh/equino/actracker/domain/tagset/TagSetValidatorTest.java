package ovh.equino.actracker.domain.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagSetValidatorTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    private static final String VALIDATION_ERROR = "TagSet invalid: %s";
    private static final String EMPTY_NAME_ERROR = "Name is empty";
    private static final String NOT_EXISTING_TAGS_ERROR = "Selected tags do not exist: %s";

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;

    private TagSetValidator validator;

    @BeforeEach
    void setUp() {
        this.validator = new TagSetValidator(tagsExistenceVerifier);
    }

    @Test
    void shouldNotFailWhenTagSetValid() {
        // given
        when(tagsExistenceVerifier.notExisting(any())).thenReturn(emptySet());
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                "tag set name",
                singletonList(new TagId()),
                !DELETED,
                validator,
                tagsExistenceVerifier
        );

        // then
        assertThatCode(() -> validator.validate(tagSet)).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenNameBlank() {
        // given
        when(tagsExistenceVerifier.notExisting(any())).thenReturn(emptySet());
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                "   ",
                singletonList(new TagId()),
                !DELETED,
                validator,
                tagsExistenceVerifier
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
        when(tagsExistenceVerifier.notExisting(any())).thenReturn(emptySet());
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                null,
                singletonList(new TagId()),
                !DELETED,
                validator,
                tagsExistenceVerifier
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(tagSet))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenTagSetContainsNotExistingTag() {
        // given
        TagId notExistingTag = new TagId();
        when(tagsExistenceVerifier.notExisting(any())).thenReturn(singleton(notExistingTag));
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                "tag set name",
                singletonList(notExistingTag),
                !DELETED,
                validator,
                tagsExistenceVerifier
        );
        List<String> validationErrors = List.of(
                NOT_EXISTING_TAGS_ERROR.formatted(singletonList(notExistingTag.id()))
        );

        // then
        assertThatThrownBy(() -> validator.validate(tagSet))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenMultipleErrorsOccurred() {
        // given
        TagId notExistingTag = new TagId();
        when(tagsExistenceVerifier.notExisting(any())).thenReturn(singleton(notExistingTag));
        TagSet tagSet = new TagSet(
                new TagSetId(),
                CREATOR,
                "",
                singletonList(notExistingTag),
                !DELETED,
                validator,
                tagsExistenceVerifier
        );
        List<String> validationErrors = List.of(
                EMPTY_NAME_ERROR,
                NOT_EXISTING_TAGS_ERROR.formatted(singletonList(notExistingTag.id()))
        );

        // then
        assertThatThrownBy(() -> validator.validate(tagSet))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));

    }
}