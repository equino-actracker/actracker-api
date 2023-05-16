package ovh.equino.actracker.domain.metric;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public record Metric(
        String name,
        MetricType type
) {

    public Metric {
        if (isBlank(name)) {
            throw new RuntimeException();
        }
        requireNonNull(type);
    }
}
