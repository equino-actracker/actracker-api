package ovh.equino.actracker.domain.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class DashboardTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String DASHBOARD_NAME = "dashboard name";
    private static final List<Chart> EMPTY_CHARTS = emptyList();
    private static final List<Share> EMPTY_SHARES = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private DashboardValidator validator;

    // TODO all should fail when non accessible to user (not found)

    @Nested
    @DisplayName("rename")
    class RenameDashboardTest {

        private static final String NEW_NAME = "new dashboard name";

        @Test
        void shouldRenameDashboard() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.rename(NEW_NAME, CREATOR);

            // then
            assertThat(dashboard.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.rename(NEW_NAME, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            User unprivilegedUser = new User(randomUUID());
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );


            // then
            assertThatThrownBy(() -> dashboard.rename(NEW_NAME, unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteDashboardTest {

        private static final String CHART_NAME = "chart name";
        private static final Set<UUID> EMPTY_TAGS = emptySet();

        @Test
        void shouldDeleteDashboardAndCharts() {
            // given
            Chart existingChart = new Chart(
                    new ChartId(),
                    CHART_NAME,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS,
                    !DELETED
            );
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    singletonList(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.delete(CREATOR);

            // then
            assertThat(dashboard.isDeleted()).isTrue();
            assertThat(dashboard.charts).allSatisfy(
                    chart -> assertThat(chart.isDeleted()).isTrue()
            );
        }

        @Test
        void shouldLeaveDashboardUnchangedWhenAlreadyDeleted() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    DELETED,
                    validator
            );

            // when
            dashboard.delete(CREATOR);

            // then
            assertThat(dashboard.isDeleted()).isTrue();
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.delete(CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            User unprivilegedUser = new User(randomUUID());
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // then
            assertThatThrownBy(() -> dashboard.delete(unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("share")
    class ShareDashboardTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldAddNewShareWithoutId() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactly(newShare);
        }

        @Test
        void shouldAddNewShareWithId() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactly(newShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithIdAlreadyExists() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithoutIdAlreadyExists() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithIdAlreadyExists() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactly(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithoutIdAlreadyExists() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactly(existingShare);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.share(newShare, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            User unprivilegedUser = new User(randomUUID());
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // then
            assertThatThrownBy(() -> dashboard.share(newShare, unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("unshare")
    class UnshareDashboardTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldUnshareTagWhenSharedWithId() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );

            // when
            dashboard.unshare(existingShare.granteeName(), CREATOR);

            // then
            assertThat(dashboard.shares).isEmpty();
        }

        @Test
        void shouldUnshareTagWhenSharedWithoutId() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );

            // when
            dashboard.unshare(existingShare.granteeName(), CREATOR);

            // then
            assertThat(dashboard.shares).isEmpty();

        }

        @Test
        void shouldLeaveSharesUnchangedWhenNotShared() {
            // given
            Share existingShare1 = new Share("%s_1".formatted(GRANTEE_NAME));
            Share existingShare2 = new Share(new User(randomUUID()), "%s_2".formatted(GRANTEE_NAME));
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    List.of(existingShare1, existingShare2),
                    !DELETED,
                    validator
            );

            // when
            dashboard.unshare(GRANTEE_NAME, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrder(existingShare1, existingShare2);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.unshare(GRANTEE_NAME, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            User unprivilegedUser = new User(randomUUID());
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    validator
            );

            // then
            assertThatThrownBy(() -> dashboard.unshare(existingShare.granteeName(), unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("addChart")
    class AddChartTest {

        private static final String CHART_NAME = "chart name";
        private static final Set<UUID> EMPTY_TAGS = emptySet();

        // TODO should fail when adding chart with not accessible tag

        @Test
        void shouldAddFirstChart() {
            // given
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    CHART_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.addChart(newChart, CREATOR);

            // then
            assertThat(dashboard.charts).containsExactly(newChart);
        }

        @Test
        void shouldAddAnotherChart() {
            // given
            Chart existingNonDeletedChart = new Chart(
                    CHART_NAME + 1,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );
            Chart existingDeletedChart = new Chart(
                    CHART_NAME + 2,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            ).deleted();

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingNonDeletedChart, existingDeletedChart),
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            Chart newChart = new Chart(CHART_NAME + 3, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);

            // when
            dashboard.addChart(newChart, CREATOR);

            // then
            assertThat(dashboard.charts)
                    .containsExactlyInAnyOrder(existingNonDeletedChart, existingDeletedChart, newChart);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.addChart(newChart, CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            User unprivilegedUser = new User(randomUUID());
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);

            // then
            assertThatThrownBy(() -> dashboard.addChart(newChart, unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("deleteChart")
    class DeleteChartTest {

        private static final String CHART_NAME = "chart name";
        private static final Set<UUID> EMPTY_TAGS = emptySet();

        @Test
        void shouldDeleteExistingChart() {
            // given
            Chart existingNonDeletedChart = new Chart(
                    CHART_NAME + 1,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );
            Chart existingDeletedChart = new Chart(
                    CHART_NAME + 2,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            ).deleted();
            Chart chartToDelete = new Chart(
                    CHART_NAME + 3,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingNonDeletedChart, existingDeletedChart, chartToDelete),
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.deleteChart(chartToDelete.id(), CREATOR);

            // then
            assertThat(dashboard.charts)
                    .extracting(Chart::id, Chart::isDeleted)
                    .containsExactlyInAnyOrder(
                            tuple(existingNonDeletedChart.id(), !DELETED),
                            tuple(existingDeletedChart.id(), DELETED),
                            tuple(chartToDelete.id(), DELETED)
                    );
        }

        @Test
        void shouldKeepChartsEmptyWhenRemovingFromEmptyCharts() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.deleteChart(new ChartId(), CREATOR);

            // then
            assertThat(dashboard.charts).isEmpty();
        }

        @Test
        void shouldKeepChartsUnchangedWhenDeletingNotExistingChart() {
            // given
            Chart existingChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    singletonList(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.deleteChart(new ChartId(), CREATOR);

            // then
            assertThat(dashboard.charts).containsExactly(existingChart);
        }

        @Test
        void shouldKeepChartsUnchangedWhenDeletingAlreadyDeletedChart() {
            // given
            Chart existingDeletedChart = new Chart(
                    CHART_NAME + 1,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            ).deleted();
            Chart existingNonDeletedChart = new Chart(
                    CHART_NAME + 2,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingNonDeletedChart, existingDeletedChart),
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // when
            dashboard.deleteChart(existingDeletedChart.id(), CREATOR);

            // then
            assertThat(dashboard.charts).containsExactlyInAnyOrder(existingDeletedChart, existingNonDeletedChart);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.deleteChart(new ChartId(), CREATOR))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            User unprivilegedUser = new User(randomUUID());
            Chart existingChart = new Chart(
                    CHART_NAME,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    validator
            );

            // then
            assertThatThrownBy(() -> dashboard.deleteChart(existingChart.id(), unprivilegedUser))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }
}
