package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tenant.TenantDto;

import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class TenantBuilder {

    private TenantDto newTenant;

    TenantBuilder() {
        newTenant = new TenantDto(randomUUID(), randomString(), randomString());
    }

    public TenantDto build() {
        return newTenant;
    }
}
