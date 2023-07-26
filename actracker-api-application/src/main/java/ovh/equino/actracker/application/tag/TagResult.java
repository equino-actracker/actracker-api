package ovh.equino.actracker.application.tag;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record TagResult(UUID id,
                        String name,
                        List<MetricResult> metrics,
                        List<String> shares) {

    public TagResult {
        metrics = requireNonNullElse(metrics, emptyList());
        shares = requireNonNullElse(shares, emptyList());
    }
}
