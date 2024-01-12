package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.stream;
import static ovh.equino.actracker.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.jpa.TestUtil.randomString;

public final class DashboardBuilder {

    private DashboardDto newDashboard;

    DashboardBuilder(TenantDto creator) {
        Chart chart1 = new Chart(
                new ChartId(nextUUID()),
                randomString(),
                GroupBy.SELF,
                AnalysisMetric.TAG_PERCENTAGE,
                Set.of(nextUUID(), nextUUID(), nextUUID()),
                false
        );
        Chart chart2 = new Chart(
                new ChartId(nextUUID()),
                randomString(),
                GroupBy.DAY,
                AnalysisMetric.TAG_DURATION,
                Set.of(nextUUID(), nextUUID(), nextUUID()),
                false
        );

        this.newDashboard = new DashboardDto(
                nextUUID(),
                creator.id(),
                randomString(),
                List.of(chart1, chart2),
                List.of(
                        new Share(new User(nextUUID()), randomString()),
                        new Share(new User(nextUUID()), randomString()),
                        new Share(new User(nextUUID()), randomString())
                ),
                false
        );

    }

    public DashboardBuilder named(String name) {
        newDashboard = new DashboardDto(
                newDashboard.id(),
                newDashboard.creatorId(),
                name,
                newDashboard.charts(),
                newDashboard.shares(),
                newDashboard.deleted()
        );
        return this;
    }

    public DashboardBuilder withCharts(Chart... charts) {
        newDashboard = new DashboardDto(
                newDashboard.id(),
                newDashboard.creatorId(),
                newDashboard.name(),
                Arrays.stream(charts).toList(),
                newDashboard.shares(),
                newDashboard.deleted()
        );
        return this;
    }

    public DashboardBuilder sharedWith(TenantDto... grantees) {
        newDashboard = new DashboardDto(
                newDashboard.id(),
                newDashboard.creatorId(),
                newDashboard.name(),
                newDashboard.charts(),
                Arrays.stream(grantees)
                        .map(grantee -> new Share(
                                        new User(grantee.id()),
                                        grantee.username()
                                )
                        )
                        .toList(),
                newDashboard.deleted()
        );
        return this;
    }

    public DashboardBuilder sharedWithNonExisting(String... granteeNames) {
        newDashboard = new DashboardDto(
                newDashboard.id(),
                newDashboard.creatorId(),
                newDashboard.name(),
                newDashboard.charts(),
                stream(granteeNames)
                        .map(Share::new)
                        .toList(),
                newDashboard.deleted()
        );
        return this;
    }

    public DashboardBuilder deleted() {
        newDashboard = new DashboardDto(
                newDashboard.id(),
                newDashboard.creatorId(),
                newDashboard.name(),
                newDashboard.charts(),
                newDashboard.shares(),
                true
        );
        return this;

    }

    public DashboardDto build() {
        return newDashboard;
    }
}
