package ovh.equino.actracker.jpa.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.JpaEntity;

@Entity
@Table(name = "tenant")
@NoArgsConstructor
@Getter
@Setter
public class TenantEntity extends JpaEntity {

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;
}
