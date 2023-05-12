package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

class ChartTypeMapper {

    static ChartBucketData.Type toBucketType(Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> ChartBucketData.Type.TAG;
            case DAY -> ChartBucketData.Type.DAY;
            case WEEK -> ChartBucketData.Type.WEEK;
        };
    }

    static Instant toRangeStart(Instant timeInRange, Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> throw new RuntimeException("Tag bucket cannot be mapped to duration");
            case WEEK -> toStartOfWeek(timeInRange);
            case DAY -> toStartOfDay(timeInRange);
        };
    }

    static Instant toRangeEnd(Instant timeInRange, Chart.GroupBy chartGroupBy) {
        return toNextRangeStart(timeInRange, chartGroupBy).minusMillis(1);
    }

    static Instant toNextRangeStart(Instant timeInRange, Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> throw new RuntimeException("Tag bucket cannot be mapped to duration");
            case WEEK -> toStartOfNextWeek(timeInRange);
            case DAY -> toStartOfNextDay(timeInRange);
        };
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
