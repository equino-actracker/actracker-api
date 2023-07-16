package ovh.equino.actracker.domain.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DashboardTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    @Mock
    private DashboardNgValidator validator;

    @Nested
    @DisplayName("rename")
    class RenameDashboardTest {

        private static final String OLD_NAME = "old dashboard name";
        private static final String NEW_NAME = "new dashboard name";

        @Test
        void shouldRenameDashboard() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    OLD_NAME,
                    emptyList(),
                    emptyList(),
                    !DELETED,
                    validator
            );

            // when
            dashboard.rename(NEW_NAME, CREATOR);

            // then
            assertThat(dashboard.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenNewNameNull() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    OLD_NAME,
                    emptyList(),
                    emptyList(),
                    !DELETED,
                    validator
            );

            // then
            assertThatThrownBy(() ->
                    dashboard.rename(null, CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNewNameBlank() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    OLD_NAME,
                    emptyList(),
                    emptyList(),
                    !DELETED,
                    validator
            );

            // then
            assertThatThrownBy(() ->
                    dashboard.rename("  ", CREATOR)
            )
                    .isInstanceOf(EntityInvalidException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteDashboardTest {

        @Test
        void shouldDeleteDashboardAndCharts() {
            // given
            Chart existingChart = new Chart(
                    new ChartId(),
                    "chart name",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet(),
                    !DELETED
            );
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    singletonList(existingChart),
                    emptyList(),
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
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    emptyList(),
                    DELETED,
                    validator
            );

            // when
            dashboard.delete(CREATOR);

            // then
            assertThat(dashboard.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("share")
    class ShareDashboardTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldAddNewShareWithoutId() {
            // given
            List<Share> existingShares = emptyList();
            Share newShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactly(newShare);
        }

        @Test
        void shouldAddNewShareWithId() {
            // given
            List<Share> existingShares = emptyList();
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactly(newShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithIdAlreadyExists() {
            // given
            List<Share> existingShares = singletonList(new Share(new User(randomUUID()), GRANTEE_NAME));
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrderElementsOf(existingShares);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithoutIdAlreadyExists() {
            // given
            List<Share> existingShares = singletonList(new Share(GRANTEE_NAME));
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrderElementsOf(existingShares);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithIdAlreadyExists() {
            // given
            List<Share> existingShares = singletonList(new Share(new User(randomUUID()), GRANTEE_NAME));
            Share newShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrderElementsOf(existingShares);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithoutIdAlreadyExists() {
            // given
            List<Share> existingShares = singletonList(new Share(GRANTEE_NAME));
            Share newShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.share(newShare, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrderElementsOf(existingShares);
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
            List<Share> existingShares = singletonList(existingShare);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
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
            List<Share> existingShares = singletonList(existingShare);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
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
            List<Share> existingShares = List.of(
                    new Share("%s_1".formatted(GRANTEE_NAME)),
                    new Share(new User(randomUUID()), "%s_2".formatted(GRANTEE_NAME))
            );
            Dashboard dashboard = new Dashboard(
                    new DashboardId(randomUUID()),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    existingShares,
                    !DELETED,
                    validator
            );

            // when
            dashboard.unshare(GRANTEE_NAME, CREATOR);

            // then
            assertThat(dashboard.shares).containsExactlyInAnyOrderElementsOf(existingShares);
        }
    }

    @Nested
    @DisplayName("addChart")
    class AddChartTest {

        @Test
        void shouldAddFirstChart() {
            // given
            Chart newChart = new Chart("new Chart", GroupBy.SELF, AnalysisMetric.METRIC_VALUE, emptySet());
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    "dashboard name",
                    emptyList(),
                    emptyList(),
                    !DELETED,
                    validator
            );

            // when
            dashboard.addChart(newChart, CREATOR);

            // then
            assertThat(dashboard.charts).containsOnly(newChart);
        }

        @Test
        void shouldAddAnotherChart() {
            // given
            Chart existingNonDeletedChart = new Chart(
                    "existingChart1",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            );
            Chart existingDeletedChart = new Chart(
                    "existingChart2",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            ).deleted();

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    "dashboard name",
                    List.of(
                            existingNonDeletedChart,
                            existingDeletedChart
                    ),
                    emptyList(),
                    !DELETED,
                    validator
            );

            Chart newChart = new Chart("new chart", GroupBy.SELF, AnalysisMetric.METRIC_VALUE, emptySet());

            // when
            dashboard.addChart(newChart, CREATOR);

            // then
            assertThat(dashboard.charts)
                    .containsExactlyInAnyOrder(existingNonDeletedChart, existingDeletedChart, newChart);
        }
    }

    @Nested
    @DisplayName("deleteChart")
    class DeleteChartTest {

        @Test
        void shouldDeleteExistingChart() {
            // given
            Chart existingNonDeletedChart = new Chart(
                    "existingChart",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            );
            Chart existingDeletedChart = new Chart(
                    "existingChart",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            ).deleted();
            Chart chartToDelete = new Chart(
                    "chart to delete",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    "dashboard name",
                    List.of(existingNonDeletedChart, existingDeletedChart, chartToDelete),
                    emptyList(),
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
                    "dashboard name",
                    emptyList(),
                    emptyList(),
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
            Chart existingChart = new Chart("chart name", GroupBy.SELF, AnalysisMetric.METRIC_VALUE, emptySet());
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    "dashboard name",
                    singletonList(existingChart),
                    emptyList(),
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
                    "deleted chart name",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            ).deleted();
            Chart existingNonDeletedChart = new Chart(
                    "non deleted chart",
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    emptySet()
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    "dashboard name",
                    List.of(existingNonDeletedChart, existingDeletedChart),
                    emptyList(),
                    !DELETED,
                    validator
            );

            // when
            dashboard.deleteChart(existingDeletedChart.id(), CREATOR);

            // then
            assertThat(dashboard.charts).containsExactlyInAnyOrder(existingDeletedChart, existingNonDeletedChart);
        }
    }
}
