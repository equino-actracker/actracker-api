package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.*;

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

    @Column(name = "metric_id")
    String metricId;

    @Column(name = "metric_value")
    BigDecimal value;
}
