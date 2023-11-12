package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.*;
import ovh.equino.actracker.repository.jpa.tag.MetricEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "metric_value")
class MetricValueEntity {

    @Id
    @Column(name = "id")
    String id;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    ActivityEntity activity;

    @ManyToOne
    @JoinColumn(name = "metric_id")
    MetricEntity metric;

    @Column(name = "metric_value")
    BigDecimal value;
}
