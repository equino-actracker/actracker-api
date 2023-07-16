package ovh.equino.actracker.domain.dashboard;

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

class DashboardValidatorTest {

    private static final User CREATOR = new User(randomUUID());
    private static final List<Chart> EMPTY_CHARTS = emptyList();
    private static final List<Share> EMPTY_SHARES = emptyList();
    private static final boolean DELETED = true;

    private static final String VALIDATION_ERROR = "Dashboard invalid: %s";
    private static final String EMPTY_NAME_ERROR = "Name is empty";

    private DashboardValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DashboardValidator();
    }

    @Test
    void shouldNotFailWhenDashboardValid() {
        // given
        Dashboard dashboard = new Dashboard(
                new DashboardId(),
                CREATOR,
                "dashboard name",
                EMPTY_CHARTS,
                EMPTY_SHARES,
                !DELETED,
                validator
        );

        // then
        assertThatCode(() -> validator.validate(dashboard)).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenNameNull() {
        // given
        Dashboard dashboard = new Dashboard(
                new DashboardId(),
                CREATOR,
                null,
                EMPTY_CHARTS,
                EMPTY_SHARES,
                !DELETED,
                validator
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(dashboard))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenNameBlank() {
        // given
        Dashboard dashboard = new Dashboard(
                new DashboardId(),
                CREATOR,
                "   ",
                EMPTY_CHARTS,
                EMPTY_SHARES,
                !DELETED,
                validator
        );
        List<String> validationErrors = List.of(EMPTY_NAME_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(dashboard))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }
}