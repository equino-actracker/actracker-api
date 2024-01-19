package ovh.equino.actracker.jpa.activity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.tag.MetricEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "metric_value")
@NoArgsConstructor
@Getter
@Setter
public class MetricValueEntity {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;

    @ManyToOne
    @JoinColumn(name = "metric_id")
    private MetricEntity metric;

    @Column(name = "metric_value")
    private BigDecimal value;
}
