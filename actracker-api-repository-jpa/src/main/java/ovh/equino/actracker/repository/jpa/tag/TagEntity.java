package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "deleted")
    boolean deleted;
}
