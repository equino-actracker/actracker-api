package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.*;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "activity")
class ActivityEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "creator_id")
    String creatorId;

    @Column(name = "title")
    String title;

    @Column(name = "start_time")
    Timestamp startTime;

    @Column(name = "end_time")
    Timestamp endTime;

    @Column(name = "comment")
    String comment;

    @ManyToMany
    @JoinTable(
            name = "activity_tag",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    Set<TagEntity> tags;

    @Column(name = "deleted")
    boolean deleted;
}
