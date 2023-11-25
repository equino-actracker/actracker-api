package ovh.equino.actracker.repository.jpa.tenant;

import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.UUID;

record TenantProjection(String id, String username, String password) {

    TenantDto toTenant() {
        return new TenantDto(UUID.fromString(id()), username(), password());
    }
}
