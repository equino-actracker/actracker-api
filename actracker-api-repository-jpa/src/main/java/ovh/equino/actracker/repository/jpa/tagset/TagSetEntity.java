package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.*;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.Set;

@Entity
@Table(name = "tag_set")
class TagSetEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "creator_id")
    String creatorId;

    @Column(name = "name")
    String name;

    @ManyToMany
    @JoinTable(
            name = "tag_set_tag",
            joinColumns = @JoinColumn(name = "tag_set_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    Set<TagEntity> tags;

    @Column(name = "deleted")
    boolean deleted;
}
