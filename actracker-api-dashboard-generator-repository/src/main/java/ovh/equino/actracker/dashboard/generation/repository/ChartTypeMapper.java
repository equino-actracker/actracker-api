package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;

class ChartTypeMapper {

    static ChartBucketData.Type toBucketType(Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> ChartBucketData.Type.TAG;
            case DAY -> ChartBucketData.Type.DAY;
            case WEEK -> ChartBucketData.Type.WEEK;
        };
    }

    static Duration toRangeDuration(Chart.GroupBy chartGroupBy) {
        return switch (chartGroupBy) {
            case TAG -> throw new RuntimeException("Tag bucket cannot be mapped to duration");
            case DAY -> Duration.ofDays(1);
            case WEEK -> Duration.ofDays(7);
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
        Instant rangeStart = toRangeStart(timeInRange, chartGroupBy);
        Duration rangeDuration = toRangeDuration(chartGroupBy);
        return rangeStart.plus(rangeDuration).minusMillis(1);
    }

    private static Instant toStartOfWeek(Instant instant) {
        TemporalAdjuster beginningOfWeekAdjuster = TemporalAdjusters.previousOrSame(MONDAY);
        return toStartOfDay(
                ZonedDateTime.ofInstant(instant, UTC)
                        .with(beginningOfWeekAdjuster)
                        .toInstant()
        );
    }

    private static Instant toStartOfDay(Instant instant) {
        return ZonedDateTime.ofInstant(instant, UTC)
                .with(HOUR_OF_DAY, 0)
                .toInstant();
    }

}
