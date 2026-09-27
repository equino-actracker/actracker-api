package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public record DashboardTestData(UUID id,
                                TenantTestData creator,
                                String name,
                                List<ChartTestData> charts,
                                List<Share> shares,
                                boolean isDeleted) {

    public static DashboardTestData minimalDashboard() {
        return new DashboardTestData(randomUUID(), aTenant(), "nameless dashboards", emptyList(), emptyList(), false);
    }

    public DashboardTestData createdBy(TenantTestData creator) {
        return new DashboardTestData(this.id, creator, this.name, this.charts, this.shares, this.isDeleted);
    }

    public DashboardTestData withId(UUID id) {
        return new DashboardTestData(id, this.creator, this.name, this.charts, this.shares, this.isDeleted);
    }

    public DashboardTestData named(String name) {
        return new DashboardTestData(this.id, this.creator, name, this.charts, this.shares, this.isDeleted);
    }

    public DashboardDto asDto() {
        var chartDtos = charts.stream().map(ChartTestData::asDto).toList();
        return new DashboardDto(id, creator.id(), name, chartDtos, shares, isDeleted);
    }

}
