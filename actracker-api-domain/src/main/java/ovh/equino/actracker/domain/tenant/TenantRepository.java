package ovh.equino.actracker.domain.tenant;

import java.util.Optional;

public interface TenantRepository {

    Optional<TenantDto> findByUsername(String username);
}
