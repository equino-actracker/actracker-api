package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;

class MonthlyChartGenerator extends TimeChartGenerator {

    MonthlyChartGenerator(ChartGenerator subChartGenerator) {
        super(subChartGenerator);
    }

    @Override
    protected ChartBucketData.Type bucketType() {
        return ChartBucketData.Type.MONTH;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return toStartOfDay(
                ZonedDateTime.ofInstant(timeInRange, UTC)
                        .with(firstDayOfMonth())
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
                        .with(firstDayOfNextMonth())
                        .toInstant()
        );
    }
}
