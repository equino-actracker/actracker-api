package ovh.equino.actracker.domain.tenant;

import java.util.UUID;

public record TenantDto(
        UUID id,
        String username,
        String password
) {
}
