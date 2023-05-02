package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;

@Entity
@Table(name = "chart")
class ChartEntity {

    @Id
    @Column(name = "id")
    String id;

    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    DashboardEntity dashboard;

    @Column(name = "name")
    String name;

    @Column(name = "group_by")
    String groupBy;
}
