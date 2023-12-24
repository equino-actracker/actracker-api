package ovh.equino.actracker.domain.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardFactoryTest {

    private static final DashboardId DASHBOARD_ID = new DashboardId();
    private static final User CREATOR = new User(randomUUID());
    private static final String DASHBOARD_NAME = "dashboard name";
    private static final Boolean DELETED = TRUE;

    @Mock
    private DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private TenantDataSource tenantDataSource;

    private DashboardFactory dashboardFactory;

    @BeforeEach
    void init() {
        dashboardFactory = new DashboardFactory(
                dashboardsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                tenantDataSource
        );
    }

    @Test
    void shouldCreateMinimalDashboard() {
        // when
        var dashboard = dashboardFactory.create(CREATOR, DASHBOARD_NAME, null, null);

        // then
        assertThat(dashboard.id()).isNotNull();
        assertThat(dashboard.name()).isEqualTo(DASHBOARD_NAME);
        assertThat(dashboard.creator()).isEqualTo(CREATOR);
        assertThat(dashboard.charts()).isEmpty();
        assertThat(dashboard.shares()).isEmpty();
    }

    @Test
    void shouldCreateFullDashboard() {
        // given
        var tag1 = new TagId();
        var tag2 = new TagId();
        var chart1 = new Chart("c1", GroupBy.SELF, AnalysisMetric.TAG_DURATION, singleton(tag1.id()));
        var chart2 = new Chart("c2", GroupBy.SELF, AnalysisMetric.TAG_DURATION, singleton(tag2.id()));
        var share1 = new Share("grantee1");
        var share2 = new Share("grantee2");
        when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any()))
                .thenReturn(emptySet());

        // when
        var dashboard = dashboardFactory.create(
                CREATOR,
                DASHBOARD_NAME,
                List.of(chart1, chart2),
                List.of(share1, share2)
        );

        // then
        assertThat(dashboard.id()).isNotNull();
        assertThat(dashboard.name()).isEqualTo(DASHBOARD_NAME);
        assertThat(dashboard.creator()).isEqualTo(CREATOR);
        assertThat(dashboard.charts()).containsExactlyInAnyOrder(chart1, chart2);
        assertThat(dashboard.shares()).containsExactlyInAnyOrder(share1, share2);
        assertThat(dashboard.deleted()).isFalse();
    }

    @Test
    void shouldCreateDashboardWithResolvedShares() {
        // given
        var grantee1Id = randomUUID();
        var grantee1 = "grantee1";
        var grantee2 = "grantee2";
        var resolvedShare = new Share(new User(grantee1Id), grantee1);
        var unresolvedShare = new Share(grantee2);
        when(tenantDataSource.findByUsername(grantee1))
                .thenReturn(Optional.of(new TenantDto(grantee1Id, "grantee1", "")));

        // when
        var tag = dashboardFactory.create(
                CREATOR,
                DASHBOARD_NAME,
                null,
                List.of(new Share(grantee1), new Share(grantee2))
        );

        // then
        assertThat(tag.shares()).containsExactlyInAnyOrder(resolvedShare, unresolvedShare);
    }

    @Test
    void shouldCreateFailWhenDashboardInvalid() {
        // given
        var invalidDashboardName = "";

        // then
        assertThatThrownBy(() -> dashboardFactory.create(CREATOR, invalidDashboardName, null, null))
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldCreateFailWhenTagNonAccessible() {
        // given
        var nonAccessibleTag = randomUUID();
        var chart = new Chart("c1", GroupBy.SELF, AnalysisMetric.TAG_DURATION, singleton(nonAccessibleTag));
        when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any()))
                .thenReturn(Set.of(new TagId(nonAccessibleTag)));

        // then
        assertThatThrownBy(() -> dashboardFactory.create(CREATOR, DASHBOARD_NAME, singleton(chart), null))
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldReconstituteDashboard() {
        // given
        var actor = new User(randomUUID());
        var shares = List.of(new Share("grantee1"), new Share("grantee2"));
        var charts = List.of(
                new Chart("c1", GroupBy.SELF, AnalysisMetric.TAG_DURATION, Set.of(randomUUID(), randomUUID())),
                new Chart("c2", GroupBy.SELF, AnalysisMetric.TAG_DURATION, Set.of(randomUUID(), randomUUID())).deleted()
        );

        // when
        Dashboard dashboard = dashboardFactory.reconstitute(
                actor,
                DASHBOARD_ID,
                CREATOR,
                DASHBOARD_NAME,
                charts,
                shares,
                DELETED
        );

        // then
        assertThat(dashboard.id()).isEqualTo(DASHBOARD_ID);
        assertThat(dashboard.name()).isEqualTo(DASHBOARD_NAME);
        assertThat(dashboard.creator()).isEqualTo(CREATOR);
        assertThat(dashboard.charts()).containsExactlyInAnyOrderElementsOf(charts);
        assertThat(dashboard.shares()).containsExactlyInAnyOrderElementsOf(shares);
    }
}