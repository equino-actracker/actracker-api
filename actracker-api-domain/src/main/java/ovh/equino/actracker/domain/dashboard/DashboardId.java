package ovh.equino.actracker.domain.dashboard;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

public record DashboardId(UUID id) {

    public DashboardId {
        if (isNull(id)) {
            throw new IllegalArgumentException("DashboardId.id must not be null");
        }
    }

    DashboardId() {
        this(randomUUID());
    }
}
