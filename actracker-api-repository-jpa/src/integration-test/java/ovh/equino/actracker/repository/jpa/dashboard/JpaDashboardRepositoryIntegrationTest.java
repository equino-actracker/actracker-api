package ovh.equino.actracker.repository.jpa.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_DURATION;
import static ovh.equino.actracker.domain.dashboard.GroupBy.SELF;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;

abstract class JpaDashboardRepositoryIntegrationTest extends JpaIntegrationTest {

    private static final boolean DELETED = Boolean.TRUE;

    private JpaDashboardRepository repository;
    private DashboardFactory dashboardFactory;

    @BeforeEach
    void init() {
        User user = new User(nextUUID());
        this.dashboardFactory = DashboardTestFactory.forUser(user);
        this.repository = new JpaDashboardRepository(entityManager, dashboardFactory);
    }

    @Test
    void shouldAddAndGetMinimalDashboard() {
        Dashboard expectedDashboard = dashboardFactory.create("dashboard name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedDashboard));
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(expectedDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(expectedDashboard);
        });
    }

    @Test
    void shouldAddAndGetFullDashboard() {
        Chart chart = new Chart(new ChartId(), "chart name", SELF, TAG_DURATION, Set.of(nextUUID()), !DELETED);
        Share notResolvedShare = new Share("grantee name");
        Share resolvedShare = new Share(new User(nextUUID()), "grantee");
        Dashboard expectedDashboard = dashboardFactory.create(
                "dashboard name",
                List.of(chart),
                List.of(notResolvedShare, resolvedShare)
        );
        inTransaction(() -> repository.add(expectedDashboard));
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(expectedDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(expectedDashboard);
        });
    }

    @Test
    void shouldAddAndGetMutatedDashboard() {
        Chart chartToDelete = new Chart(new ChartId(), "chart to delete", SELF, TAG_DURATION, Set.of(nextUUID()), !DELETED);
        Share notResolvedShareToDelete = new Share("not resolved share to delete");
        Share resolvedShareToDelete = new Share(new User(nextUUID()), "resolved share to delete");

        Chart newChart = new Chart(new ChartId(), "new chart", SELF, TAG_DURATION, Set.of(nextUUID()), !DELETED);
        Share newNotResolvedShare = new Share("new not resolved share");
        Share newResolvedShare = new Share(new User(nextUUID()), "new resolved share");

        Dashboard expectedDashboard = dashboardFactory.create(
                "old dashboard name",
                List.of(chartToDelete),
                List.of(notResolvedShareToDelete, resolvedShareToDelete)
        );

        expectedDashboard.rename("new dashboard name");
        expectedDashboard.deleteChart(chartToDelete.id());
        expectedDashboard.addChart(newChart);
        expectedDashboard.unshare(notResolvedShareToDelete.granteeName());
        expectedDashboard.unshare(resolvedShareToDelete.granteeName());
        expectedDashboard.share(newNotResolvedShare);
        expectedDashboard.share(newResolvedShare);
        expectedDashboard.delete();

        inTransaction(() -> repository.add(expectedDashboard));
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(expectedDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(expectedDashboard);
        });

    }

    @Test
    void shouldNotGetNotExistingDashboard() {
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(new DashboardId(randomUUID()));
            assertThat(foundDashboard).isEmpty();
        });
    }

    @Test
    void shouldUpdateDashboard() {
        Dashboard expectedDashboard = dashboardFactory.create("old name", emptyList(), emptyList());
        inTransaction(() -> repository.add(expectedDashboard));
        inTransaction(() -> {
            // TODO extend update with additional fields
            Dashboard dashboard = repository.get(expectedDashboard.id()).get();
            expectedDashboard.delete();
            dashboard.delete();
            repository.save(dashboard);
        });
        inTransaction(() -> {
            Optional<Dashboard> foundDashboard = repository.get(expectedDashboard.id());
            assertThat(foundDashboard).get().usingRecursiveComparison().isEqualTo(expectedDashboard);
        });
    }
}
