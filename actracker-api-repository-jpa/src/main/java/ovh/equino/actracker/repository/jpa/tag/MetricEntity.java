package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.*;

@Entity
@Table(name = "metric")
class MetricEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "creator_id", insertable = false, updatable = false)
    String creatorId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "tag_id", referencedColumnName = "id"),
            @JoinColumn(name = "creator_id", referencedColumnName = "creator_id")
    })
    TagEntity tag;

    @Column(name = "name")
    String name;

    @Column(name = "type")
    String type;

    @Column(name = "deleted")
    boolean deleted;
}
