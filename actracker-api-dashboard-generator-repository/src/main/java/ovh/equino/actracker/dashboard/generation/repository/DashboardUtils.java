package ovh.equino.actracker.dashboard.generation.repository;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.LocalTime.MAX;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
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

    static Instant startOfDay(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, UTC)
                .toLocalDate()
                .atStartOfDay()
                .atZone(UTC)
                .toInstant();
    }

    static Instant endOfDay(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, UTC)
                .toLocalDate()
                .atTime(MAX)
                .atZone(UTC)
                .toInstant();
    }

    static Instant startOfNextDay(Instant instant) {
        if (instant == null) {
            return null;
        }
        return startOfDay(instant).plus(1, DAYS);
    }
}
