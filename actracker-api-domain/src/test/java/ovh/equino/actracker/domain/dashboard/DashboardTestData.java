package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public final class DashboardTestData {

    private UUID id = randomUUID();
    private TenantTestData creator = aTenant();
    private String name = "nameless dashboard";

    public static DashboardTestData aDashboard() {
        return new DashboardTestData();
    }

    public DashboardTestData createdBy(TenantTestData creator) {
        this.creator = creator;
        return this;
    }

    public DashboardTestData withId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID id() {
        return id;
    }

    public DashboardTestData named(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public DashboardDto asDto() {
        return new DashboardDto(id, creator.id(), name, emptyList(), emptyList(), false);
    }
}
