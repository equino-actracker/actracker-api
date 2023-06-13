package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;

@Entity
@Table(name = "dashboard_share")
class DashboardShareEntity {

    @Id
    @Column(name = "id")
    String id;

    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    DashboardEntity dashboard;

    @Column(name = "grantee_id")
    String granteeId;

    @Column(name = "grantee_name")
    String granteeName;

}
