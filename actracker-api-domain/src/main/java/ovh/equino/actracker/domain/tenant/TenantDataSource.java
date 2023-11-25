package ovh.equino.actracker.domain.tenant;

import java.util.Optional;

public interface TenantDataSource {

    Optional<TenantDto> findByUsername(String username);
}
