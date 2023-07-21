package ovh.equino.actracker.application.tag;

import java.util.List;
import java.util.UUID;

public record TagResult(UUID id,
                        String name,
                        List<MetricResult> metrics,
                        List<String> shares) {
}
