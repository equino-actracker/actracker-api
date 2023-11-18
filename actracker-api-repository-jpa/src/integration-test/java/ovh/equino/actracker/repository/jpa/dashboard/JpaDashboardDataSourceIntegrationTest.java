package ovh.equino.actracker.repository.jpa.dashboard;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

abstract class JpaDashboardDataSourceIntegrationTest extends JpaIntegrationTest {

    private static final IntegrationTestConfiguration testConfiguration = new IntegrationTestConfiguration();
    private static User searcher;
    private JpaDashboardDataSource dataSource;

    @BeforeEach
    void init() throws SQLException {
        this.dataSource = new JpaDashboardDataSource(entityManager);
        testConfiguration.persistIn(database());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessibleDashboards")
    void shouldFindAccessibleDashboard(String testName, DashboardId dashboardId, DashboardDto expectedDashboard) {
        inTransaction(() -> {
            Optional<DashboardDto> foundDashboard = dataSource.find(dashboardId, searcher);
            assertThat(foundDashboard).isPresent();
            assertThat(foundDashboard.get())
                    .usingRecursiveComparison()
                    .ignoringFields("charts", "shares")
                    .isEqualTo(expectedDashboard);
            assertThat(foundDashboard.get().charts()).containsExactlyInAnyOrderElementsOf(expectedDashboard.charts());
            assertThat(foundDashboard.get().shares()).containsExactlyElementsOf(expectedDashboard.shares());
        });
    }

    private static Stream<Arguments> accessibleDashboards() {
        return testConfiguration.dashboards.accessibleFor(searcher)
                .stream()
                .map(dashboard -> Arguments.of(
                        dashboard.name(), new DashboardId(dashboard.id()), dashboard
                ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inaccessibleDashboards")
    void shouldNotFindInaccessibleDashboard(String testName, DashboardId dashboardId) {
        inTransaction(() -> {
            Optional<DashboardDto> foundDashboard = dataSource.find(dashboardId, searcher);
            assertThat(foundDashboard).isNotPresent();
        });
    }

    private static Stream<Arguments> inaccessibleDashboards() {
        return testConfiguration.dashboards.inaccessibleFor(searcher)
                .stream()
                .map(dashboard -> Arguments.of(
                        dashboard.name(), new DashboardId(dashboard.id())
                ));
    }

    @BeforeAll
    static void setUp() {
        TenantDto searcherTenant = newUser().build();
        TenantDto sharingTenant = newUser().build();
        TenantDto grantee1 = newUser().build();
        TenantDto grantee2 = newUser().build();
        searcher = new User(searcherTenant.id());

        testConfiguration.addUser(searcherTenant);
        testConfiguration.addUser(sharingTenant);

        testConfiguration.dashboards.add(newDashboard(searcherTenant)
                .named("Accessible own dashboard not shared without charts")
                .sharedWith()
                .withCharts()
                .build()
        );

        testConfiguration.dashboards.add(newDashboard(searcherTenant)
                .named("Accessible own dashboard shared with charts")
                .sharedWith(grantee1, grantee2)
                //.withCharts()
                .build()
        );

        testConfiguration.dashboards.add(newDashboard(searcherTenant)
                .named("Accessible own dashboard with deleted chart shared with non existing users")
                //.withCharts()
                .sharedWithNonExisting("nonExistingGrantee1", "nonExistingGrantee2")
                .build()
        );

        testConfiguration.dashboards.add(newDashboard(sharingTenant)
                        .named("Accessible shared dashboard with charts")
//.withCharts()
                        .sharedWith(searcherTenant, grantee1, grantee2)
                        .build()
        );

        testConfiguration.dashboards.add(newDashboard(searcherTenant)
                .named("Inaccessible own deleted dashboard")
                .deleted()
                .build()
        );

        testConfiguration.dashboards.add(newDashboard(sharingTenant)
                .named("Inaccessible shared deleted dashboard")
                .sharedWith(searcherTenant)
                .deleted()
                .build()
        );

        testConfiguration.dashboards.add(newDashboard(sharingTenant)
                .named("Inaccessible foreign dashboard")
                .build()
        );

        testConfiguration.dashboards.addTransient(newDashboard(searcherTenant)
                .named("Inaccessible not persisted dashboard")
                .build()
        );
    }
}
