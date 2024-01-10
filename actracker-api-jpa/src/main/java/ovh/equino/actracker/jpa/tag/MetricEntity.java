package ovh.equino.actracker.jpa.tag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "metric")
@NoArgsConstructor
@Getter
@Setter
public class MetricEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "creator_id", insertable = false, updatable = false)
    private String creatorId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "tag_id", referencedColumnName = "id"),
            @JoinColumn(name = "creator_id", referencedColumnName = "creator_id")
    })
    private TagEntity tag;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "deleted")
    private boolean deleted;
}
