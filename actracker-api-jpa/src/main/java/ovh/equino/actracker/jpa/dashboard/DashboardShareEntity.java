package ovh.equino.actracker.jpa.dashboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.JpaEntity;

@Entity
@Table(name = "dashboard_share")
@NoArgsConstructor
@Getter
@Setter
public class DashboardShareEntity extends JpaEntity {

    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    private DashboardEntity dashboard;

    @Column(name = "grantee_id")
    private String granteeId;

    @Column(name = "grantee_name")
    private String granteeName;

}
