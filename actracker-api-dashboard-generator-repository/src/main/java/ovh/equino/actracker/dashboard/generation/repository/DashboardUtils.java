package ovh.equino.actracker.dashboard.generation.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

import static java.util.Arrays.stream;

interface DashboardUtils {

    static Instant earliestOf(Instant... candidates) {
        return stream(candidates)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
    }

    static Instant latestOf(Instant... candidates) {
        return stream(candidates)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
    }

    static Instant beginningOfDay(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault())
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    static Instant endOfDay(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault())
                .atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }
}
