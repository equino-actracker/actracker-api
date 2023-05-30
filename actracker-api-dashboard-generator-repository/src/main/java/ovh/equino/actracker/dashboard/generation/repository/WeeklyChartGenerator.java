package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

class WeeklyChartGenerator extends TimeChartGenerator {

    WeeklyChartGenerator(ChartGenerator subChartGenerator) {
        super(subChartGenerator);
    }

    @Override
    protected ChartBucketData.Type bucketType() {
        return ChartBucketData.Type.WEEK;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(timeInRange, UTC)
                        .with(previousOrSame(MONDAY))
                        .toInstant()
        );
    }

    @Override
    protected Instant toRangeEnd(Instant timeInRange) {
        return toNextRangeStart(timeInRange).minusMillis(1);
    }

    @Override
    protected Instant toNextRangeStart(Instant timeInRange) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(timeInRange, UTC)
                        .with(next(MONDAY))
                        .toInstant()
        );
    }
}
