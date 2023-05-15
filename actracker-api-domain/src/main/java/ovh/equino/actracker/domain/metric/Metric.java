package ovh.equino.actracker.domain.metric;

import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNull;

public record Metric(
        String name,
        MetricType type
) {

    public Metric {
        if(StringUtils.isBlank(name)) {
            throw new RuntimeException();
        }
        requireNonNull(type);
    }
}
