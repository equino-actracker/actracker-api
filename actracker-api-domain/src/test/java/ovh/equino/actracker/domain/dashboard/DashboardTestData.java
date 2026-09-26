package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

// TODO add missing fields
public record DashboardTestData(UUID id,
                                TenantTestData creator,
                                String name) {

    public static DashboardTestData minimalDashboard() {
        return new DashboardTestData(randomUUID(), aTenant(), "nameless dashboard");
    }

    public DashboardTestData createdBy(TenantTestData creator) {
        return new DashboardTestData(this.id, creator, this.name);
    }

    public DashboardTestData withId(UUID id) {
        return new DashboardTestData(id, this.creator, this.name);
    }

    public DashboardTestData named(String name) {
        return new DashboardTestData(this.id, this.creator, name);
    }

    public DashboardDto asDto() {
        return new DashboardDto(id, creator.id(), name, emptyList(), emptyList(), false);
    }
}
