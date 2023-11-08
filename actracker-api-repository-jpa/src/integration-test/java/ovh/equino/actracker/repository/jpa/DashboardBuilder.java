package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartId;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.GroupBy;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_DURATION;
import static ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_PERCENTAGE;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class DashboardBuilder {

    private DashboardDto newDashboard;

    DashboardBuilder(TenantDto creator) {
        Chart chart1 = new Chart(
                new ChartId(randomUUID()),
                randomString(),
                GroupBy.SELF,
                TAG_PERCENTAGE,
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                false
        );
        Chart chart2 = new Chart(
                new ChartId(randomUUID()),
                randomString(),
                GroupBy.DAY,
                TAG_DURATION,
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                false
        );

        this.newDashboard = new DashboardDto(
                randomUUID(),
                creator.id(),
                randomString(),
                List.of(chart1, chart2),
                List.of(
                        new Share(new User(randomUUID()), randomString()),
                        new Share(new User(randomUUID()), randomString()),
                        new Share(new User(randomUUID()), randomString())
                ),
                false
        );

    }

    public DashboardDto build() {
        return newDashboard;
    }
}
