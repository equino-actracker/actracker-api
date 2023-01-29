package ovh.equino.actracker.repository.jpa.tenant;

import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.UUID;

class TenantMapper {

    TenantDto toDto(TenantEntity entity) {
        return new TenantDto(
                UUID.fromString(entity.id),
                entity.username,
                entity.password
        );
    }

    TenantEntity toEntity(TenantDto dto) {
        TenantEntity entity = new TenantEntity();
        entity.id = dto.id().toString();
        entity.username = dto.username();
        entity.password = dto.password();
        return entity;
    }
}
