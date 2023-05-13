package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.TemporalAdjusters.*;

class ChartTypeMapper {

    static ChartBucketData.Type toBucketType(Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> ChartBucketData.Type.TAG;
            case DAY -> ChartBucketData.Type.DAY;
            case WEEK -> ChartBucketData.Type.WEEK;
            case MONTH -> ChartBucketData.Type.MONTH;
            case WEEKEND -> ChartBucketData.Type.WEEKEND;
        };
    }

    static Instant toRangeStart(Instant timeInRange, Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> throw new RuntimeException("Tag bucket cannot be mapped to range");
            case DAY -> toStartOfDay(timeInRange);
            case WEEK -> toStartOfWeek(timeInRange);
            case MONTH -> toStartOfMonth(timeInRange);
            case WEEKEND -> toStartOfWeekend(timeInRange);
        };
    }

    static Instant toRangeEnd(Instant timeInRange, Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> throw new RuntimeException("Tag bucket cannot be mapped to range");
            case DAY, WEEK, MONTH -> toNextRangeStart(timeInRange, chartGroupBy).minusMillis(1);
            case WEEKEND -> toEndOfWeekend(timeInRange);
        };
    }

    static Instant toNextRangeStart(Instant timeInRange, Chart.GroupBy chartGroupBy) {
        Instant rangeStart = toRangeStart(timeInRange, chartGroupBy);
        return switch (chartGroupBy) {
            case TAG -> throw new RuntimeException("Tag bucket cannot be mapped to range");
            case DAY -> toStartOfNextDay(rangeStart);
            case WEEK -> toStartOfNextWeek(rangeStart);
            case MONTH -> toStartOfNextMonth(rangeStart);
            case WEEKEND -> toStartOfNextWeekend(rangeStart);
        };
    }

    private static Instant toStartOfWeekend(Instant instant) {
        return ZonedDateTime.ofInstant(instant, UTC)
                .with(previousOrSame(FRIDAY))
                .with(HOUR_OF_DAY, 18)
                .toInstant();
    }

    private static Instant toStartOfNextWeekend(Instant instant) {
        return ZonedDateTime.ofInstant(instant, UTC)
                .with(next(FRIDAY))
                .with(HOUR_OF_DAY, 18)
                .toInstant();
    }

    private static Instant toEndOfWeekend(Instant instant) {
        Instant startOfWeekend = toStartOfWeekend(instant);
        return toStartOfNextWeek(startOfWeekend).minusMillis(1);
    }

    private static Instant toStartOfMonth(Instant instant) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(instant, UTC)
                        .with(firstDayOfMonth())
                        .toInstant()
        );
    }

    private static Instant toStartOfNextMonth(Instant instant) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(instant, UTC)
                        .with(firstDayOfNextMonth())
                        .toInstant()
        );
    }

    private static Instant toStartOfWeek(Instant instant) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(instant, UTC)
                        .with(previousOrSame(MONDAY))
                        .toInstant()
        );
    }

    private static Instant toStartOfNextWeek(Instant instant) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(instant, UTC)
                        .with(next(MONDAY))
                        .toInstant()
        );
    }

    private static Instant toStartOfDay(Instant instant) {
        return ZonedDateTime.ofInstant(instant, UTC)
                .with(HOUR_OF_DAY, 0)
                .toInstant();
    }

    private static Instant toStartOfNextDay(Instant instant) {
        return toStartOfDay(instant)
                .plus(1, DAYS);
    }

}
