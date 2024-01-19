package ovh.equino.actracker.jpa.activity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.JpaEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "activity")
@NoArgsConstructor
@Getter
@Setter
public class ActivityEntity extends JpaEntity {

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "title")
    private String title;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Column(name = "comment")
    private String comment;

    @ManyToMany
    @JoinTable(
            name = "activity_tag",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetricValueEntity> metricValues;

    @Column(name = "deleted")
    private boolean deleted;
}
