package ovh.equino.actracker.repository.jpa.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant")
class TenantEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "username")
    String username;

    @Column(name = "password")
    String password;
}
