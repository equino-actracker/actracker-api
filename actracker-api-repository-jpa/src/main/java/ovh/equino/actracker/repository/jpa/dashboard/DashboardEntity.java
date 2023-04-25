package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "dashboard")
class DashboardEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "creator_id")
    String creatorId;

    @Column(name = "name")
    String name;

    @OneToMany(mappedBy = "dashboard", cascade = ALL, orphanRemoval = true)
    List<ChartEntity> charts;

    @Column(name = "deleted")
    boolean deleted;
}
