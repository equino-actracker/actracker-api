package ovh.equino.actracker.main.springboot.configuration.security;

import org.springframework.stereotype.Repository;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.security.spring.basic.identity.Identity;
import ovh.equino.security.spring.basic.identity.IdentityRepository;

import java.util.Optional;

@Repository
class ActrackerIdentityRepository implements IdentityRepository {

    private final TenantDataSource tenantDataSource;

    ActrackerIdentityRepository(TenantDataSource tenantDataSource) {
        this.tenantDataSource = tenantDataSource;
    }

    @Override
    public Optional<Identity> loadByUsername(String username) {
        return tenantDataSource.findByUsername(username)
                .map(this::toIdentity);
    }

    private Identity toIdentity(TenantDto dto) {
        return new Identity(
                dto.id().toString(),
                dto.username(),
                dto.password()
        );
    }
}
