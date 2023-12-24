package ovh.equino.actracker.domain.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.user.User;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardsAccessibilityVerifierTest {

    private static final User USER = null;
    private static final DashboardDto ACCESSIBLE_DASHBOARD = new DashboardDto(
            randomUUID(),
            randomUUID(),
            "accessible dashboard",
            emptyList(),
            emptyList(),
            false
    );

    @Mock
    private DashboardDataSource dashboardDataSource;
    private DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier;

    @BeforeEach
    void init() {
        dashboardsAccessibilityVerifier = new DashboardsAccessibilityVerifier(dashboardDataSource);
    }

    @Test
    void shouldConfirmDashboardAccessible() {
        // given
        when(dashboardDataSource.find(any(), any())).thenReturn(Optional.of(ACCESSIBLE_DASHBOARD));

        // when
        boolean isAccessible = dashboardsAccessibilityVerifier.isAccessibleFor(USER, new DashboardId());

        // then
        assertThat(isAccessible).isTrue();
    }

    @Test
    void shouldConfirmDashboardInaccessible() {
        // given
        when(dashboardDataSource.find(any(), any())).thenReturn(Optional.empty());

        // when
        boolean isAccessible = dashboardsAccessibilityVerifier.isAccessibleFor(USER, new DashboardId());

        // then
        assertThat(isAccessible).isFalse();
    }

}