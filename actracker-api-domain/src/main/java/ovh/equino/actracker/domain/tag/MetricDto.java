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
    public MetricDto(UUID id, String name, MetricType type) {
        this(id, null, name, type, false);
    }

    public MetricDto(String name, MetricType type) {
        this(null, name, type);
    }
}
