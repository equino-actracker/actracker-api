package ovh.equino.actracker.dashboard.generation.repository;

import java.math.BigDecimal;
import java.time.Duration;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.isNull;

final class PercentDurationProjector implements DurationProjector {

    @Override
    public BigDecimal project(Duration measuredDuration, Duration totalDuration) {
        if (isNull(measuredDuration) || isNull(totalDuration) || totalDuration.equals(Duration.ZERO)) {
            return BigDecimal.ZERO;
        }
        BigDecimal measuredSeconds = BigDecimal.valueOf(measuredDuration.toSeconds());
        BigDecimal totalSeconds = BigDecimal.valueOf(totalDuration.toSeconds());
        return measuredSeconds.divide(totalSeconds, 4, HALF_UP);
    }
}
