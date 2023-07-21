package ovh.equino.actracker.application.tag;

import java.util.UUID;

public record MetricResult(UUID id,
                           String name,
                           String type) {
}
