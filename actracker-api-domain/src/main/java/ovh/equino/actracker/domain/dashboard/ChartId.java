package ovh.equino.actracker.domain.dashboard;

import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record ChartId(UUID id) {

    public ChartId {
        requireNonNull(id);
    }

    public ChartId(String id) {
        this(UUID.fromString(id));
    }

    public ChartId() {
        this(randomUUID());
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
