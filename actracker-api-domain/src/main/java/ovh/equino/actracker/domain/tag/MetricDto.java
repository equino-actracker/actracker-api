package ovh.equino.actracker.domain.tag;

import java.util.UUID;

public record MetricDto(
        UUID id,
        UUID creatorId,
        String name,
        MetricType type,
        boolean deleted
) {

    // Constructor for data provided from input
    public MetricDto(String name, MetricType type) {
        this(null, null, name, type, false);
    }
}
