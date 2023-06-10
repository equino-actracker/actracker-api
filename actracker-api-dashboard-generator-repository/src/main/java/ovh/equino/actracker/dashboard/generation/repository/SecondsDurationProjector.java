package ovh.equino.actracker.dashboard.generation.repository;

import java.math.BigDecimal;
import java.time.Duration;

class SecondsDurationProjector implements DurationProjector {
    @Override
    public BigDecimal project(Duration measuredDuration, Duration totalDuration) {
        if (measuredDuration == null) {
            return null;
        }
        return new BigDecimal(measuredDuration.toSeconds());
    }
}
