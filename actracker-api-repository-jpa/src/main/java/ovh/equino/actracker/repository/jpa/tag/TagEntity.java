package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.*;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "tag")
public class TagEntity {

    @Id
    @Column(name = "id")
    public String id;

    @Column(name = "creator_id")
    String creatorId;

    @Column(name = "name")
    String name;

    @OneToMany(mappedBy = "tag", cascade = ALL, orphanRemoval = true)
    List<MetricEntity> metrics;

    @OneToMany(mappedBy = "tag", cascade = ALL, orphanRemoval = true)
    List<TagShareEntity> shares;

    @Column(name = "deleted")
    boolean deleted;
}
