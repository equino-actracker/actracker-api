package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

class WeekendlyChartGenerator extends TimeChartGenerator {

    WeekendlyChartGenerator(ChartGenerator subChartGenerator) {
        super(subChartGenerator);
    }

    @Override
    protected ChartBucketData.Type bucketType() {
        return ChartBucketData.Type.WEEKEND;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return ZonedDateTime.ofInstant(timeInRange, UTC)
                .with(previousOrSame(FRIDAY))
                .with(HOUR_OF_DAY, 18)
                .toInstant();
    }

    @Override
    protected Instant toRangeEnd(Instant timeInRange) {
        Instant rangeStart = toRangeStart(timeInRange);
        return toStartOfDay(
                ZonedDateTime.ofInstant(rangeStart, UTC)
                        .with(next(MONDAY))
                        .toInstant()
        );
    }

    @Override
    protected Instant toNextRangeStart(Instant timeInRange) {
        return ZonedDateTime.ofInstant(timeInRange, UTC)
                .with(next(FRIDAY))
                .with(HOUR_OF_DAY, 18)
                .toInstant();
    }
}
