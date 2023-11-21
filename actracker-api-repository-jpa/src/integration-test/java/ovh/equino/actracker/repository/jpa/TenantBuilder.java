package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tenant.TenantDto;

import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class TenantBuilder {

    private TenantDto newTenant;

    TenantBuilder() {
        newTenant = new TenantDto(nextUUID(), randomString(), randomString());
    }

    public TenantDto build() {
        return newTenant;
    }
}
