package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.*;

@Entity
@Table(name = "metric")
class MetricEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "creator_id")
    String creatorId;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    TagEntity tag;

    @Column(name = "name")
    String name;

    @Column(name = "type")
    String type;

    @Column(name = "deleted")
    boolean deleted;
}
