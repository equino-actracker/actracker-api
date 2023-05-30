package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.ChartBucketData;

import java.time.Instant;

class DailyChartGenerator extends TimeChartGenerator {

    DailyChartGenerator(ChartGenerator subChartGenerator) {
        super(subChartGenerator);
    }

    @Override
    protected ChartBucketData.Type bucketType() {
        return ChartBucketData.Type.DAY;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return toStartOfDay(timeInRange);
    }

    @Override
    protected Instant toRangeEnd(Instant timeInRange) {
        return toNextRangeStart(timeInRange).minusMillis(1);
    }

    @Override
    protected Instant toNextRangeStart(Instant timeInRange) {
        return toStartOfNextDay(timeInRange);
    }
}
