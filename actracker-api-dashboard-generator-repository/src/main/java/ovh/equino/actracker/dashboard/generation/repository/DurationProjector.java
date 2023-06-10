package ovh.equino.actracker.dashboard.generation.repository;

import java.math.BigDecimal;
import java.time.Duration;

interface DurationProjector {

    BigDecimal project(Duration measuredDuration, Duration totalDuration);
}
