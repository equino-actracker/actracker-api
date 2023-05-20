package ovh.equino.actracker.domain.tag;

import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record MetricId(
        UUID id
) {

    public MetricId {
        requireNonNull(id);
    }

    public MetricId() {
        this(randomUUID());
    }
}
