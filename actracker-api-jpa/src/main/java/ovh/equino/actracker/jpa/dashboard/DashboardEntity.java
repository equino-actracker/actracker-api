package ovh.equino.actracker.jpa.dashboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "dashboard")
@NoArgsConstructor
@Getter
@Setter
public class DashboardEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "dashboard", cascade = ALL, orphanRemoval = true)
    private List<ChartEntity> charts;

    @OneToMany(mappedBy = "dashboard", cascade = ALL, orphanRemoval = true)
    private List<DashboardShareEntity> shares;

    @Column(name = "deleted")
    private boolean deleted;
}
