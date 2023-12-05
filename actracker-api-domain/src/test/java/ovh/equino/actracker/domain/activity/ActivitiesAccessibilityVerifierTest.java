package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.user.User;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitiesAccessibilityVerifierTest {

    private static final User USER = null;
    private static final ActivityDto ACCESSIBLE_ACTIVITY = new ActivityDto(
            randomUUID(),
            randomUUID(),
            "accessible activity",
            null,
            null,
            null,
            emptySet(),
            emptyList(),
            false
    );

    @Mock
    private ActivityDataSource activityDataSource;
    private ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier;

    @BeforeEach
    void init() {
        activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource, USER);
    }

    @Test
    void shouldConfirmActivityAccessible() {
        // given
        when(activityDataSource.find(any(), any())).thenReturn(Optional.of(ACCESSIBLE_ACTIVITY));

        // when
        boolean isAccessible = activitiesAccessibilityVerifier.isAccessible(new ActivityId());

        // then
        assertThat(isAccessible).isTrue();
    }

    @Test
    void shouldConfirmActivityInaccessible() {
        // given
        when(activityDataSource.find(any(), any())).thenReturn(Optional.empty());

        // when
        boolean isAccessible = activitiesAccessibilityVerifier.isAccessible(new ActivityId());

        // then
        assertThat(isAccessible).isFalse();
    }
}