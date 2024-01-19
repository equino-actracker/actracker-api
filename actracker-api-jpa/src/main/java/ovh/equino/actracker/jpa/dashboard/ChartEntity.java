package ovh.equino.actracker.jpa.dashboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.tag.TagEntity;

import java.util.Set;

@Entity
@Table(name = "chart")
@NoArgsConstructor
@Getter
@Setter
public class ChartEntity {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    private DashboardEntity dashboard;

    @Column(name = "name")
    private String name;

    @Column(name = "group_by")
    private String groupBy;

    @Column(name = "metric")
    private String metric;

    @ManyToMany
    @JoinTable(
            name = "chart_tag",
            joinColumns = @JoinColumn(name = "chart_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags;

    @Column(name = "deleted")
    private boolean deleted;
}
