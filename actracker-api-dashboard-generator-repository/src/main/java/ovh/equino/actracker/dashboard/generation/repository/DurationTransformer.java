package ovh.equino.actracker.dashboard.generation.repository;

import java.math.BigDecimal;
import java.time.Duration;

interface DurationTransformer {

    BigDecimal transform(Duration measuredDuration, Duration totalDuration);
}
