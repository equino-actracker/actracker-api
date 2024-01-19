package ovh.equino.actracker.jpa.tagset;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.JpaEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;

import java.util.Set;

@Entity
@Table(name = "tag_set")
@NoArgsConstructor
@Getter
@Setter
public class TagSetEntity extends JpaEntity {

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "name")
    private String name;

    @ManyToMany
    @JoinTable(
            name = "tag_set_tag",
            joinColumns = @JoinColumn(name = "tag_set_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags;

    @Column(name = "deleted")
    private boolean deleted;
}
