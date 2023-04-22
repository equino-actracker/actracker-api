package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "deleted")
    boolean deleted;
}
