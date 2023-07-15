package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagValidatorTest {

    private static final User CREATOR = new User(randomUUID());
    private static final List<Metric> EMPTY_METRICS = emptyList();
    private static final List<Share> EMPTY_SHARES = emptyList();
    private static final boolean DELETED = true;

    private static final String VALIDATION_ERROR = "Tag invalid: %s";
    private static final String EMPTY_NAME_ERROR = "Name is empty";

    private TagValidator validator;

    @BeforeEach
    void setUp() {
        this.validator = new TagValidator();
    }

    @Test
    void shouldNotFailWhenTagValid() {
        // given
        Tag tag = new Tag(
                new TagId(),
                CREATOR,
                "tag name",
                EMPTY_METRICS,
                EMPTY_SHARES,
                !DELETED,
                validator
        );

        // then
        assertThatCode(() -> validator.validate(tag)).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenNameNull() {
        // given
        Tag tag = new Tag(
                new TagId(),
                CREATOR,
                null,
                EMPTY_METRICS,
                EMPTY_SHARES,
                !DELETED,
                validator
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(tag))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenNameBlank() {
        // given
        Tag tag = new Tag(
                new TagId(),
                CREATOR,
                "   ",
                EMPTY_METRICS,
                EMPTY_SHARES,
                !DELETED,
                validator
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(tag))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }
}