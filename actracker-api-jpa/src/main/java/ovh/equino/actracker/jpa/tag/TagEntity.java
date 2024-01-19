package ovh.equino.actracker.jpa.tag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "tag")
@NoArgsConstructor
@Getter
@Setter
public class TagEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "tag", cascade = ALL, orphanRemoval = true)
    private List<MetricEntity> metrics;

    @OneToMany(mappedBy = "tag", cascade = ALL, orphanRemoval = true)
    private List<TagShareEntity> shares;

    @Column(name = "deleted")
    private boolean deleted;
}
