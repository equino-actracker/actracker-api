package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;

@Entity
@Table(name = "activities_duration_by_tag")
@NamedQuery(name = "findAll", query="select b from TagBucketEntity b")
class TagBucketEntity {

    @Id
    @Column(name = "tag_id")
    String tagId;

    @Column(name = "tag_duration")
    BigDecimal durationSeconds;

    @Column(name = "measured_percentage")
    BigDecimal percentage;
}
