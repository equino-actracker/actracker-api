package ovh.equino.actracker.dashboard.generation.repository;

import java.math.BigDecimal;
import java.time.Duration;

class SecondsDurationTransformer implements DurationTransformer {
    @Override
    public BigDecimal transform(Duration measuredDuration, Duration totalDuration) {
        if (measuredDuration == null) {
            return null;
        }
        return new BigDecimal(measuredDuration.toSeconds());
    }
}
