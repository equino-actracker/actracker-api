package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.Set;

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

    @Column(name = "metric")
    String metric;

    @ManyToMany
    @JoinTable(
            name = "chart_tag",
            joinColumns = @JoinColumn(name = "chart_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    Set<TagEntity> tags;

    @Column(name = "deleted")
    boolean deleted;
}
