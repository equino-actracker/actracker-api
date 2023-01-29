package ovh.equino.actracker.main.springboot.configuration.security;

import org.springframework.stereotype.Repository;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.security.spring.basic.identity.Identity;
import ovh.equino.security.spring.basic.identity.IdentityRepository;

import java.util.Optional;

@Repository
class ActrackerIdentityRepository implements IdentityRepository {

    private final TenantRepository tenantRepository;

    ActrackerIdentityRepository(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Optional<Identity> loadByUsername(String username) {
        return tenantRepository.findByUsername(username)
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
