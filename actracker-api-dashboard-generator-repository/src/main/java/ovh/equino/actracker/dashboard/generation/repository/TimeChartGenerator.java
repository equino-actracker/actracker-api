package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.BucketType;
import ovh.equino.actracker.domain.dashboard.generation.ChartBucketData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardChartData;
import ovh.equino.actracker.domain.tag.TagDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class TimeChartGenerator extends ChartGenerator {

    private final ChartGeneratorSupplier subChartGeneratorSupplier;

    protected TimeChartGenerator(Chart chartDefinition,
                                 Instant rangeStart,
                                 Instant rangeEnd,
                                 Collection<ActivityDto> activities,
                                 Collection<TagDto> tags,
                                 ChartGeneratorSupplier subChartGeneratorSupplier) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags);
        this.subChartGeneratorSupplier = subChartGeneratorSupplier;
    }

    @Override
    DashboardChartData generate() {

        List<ChartBucketData> timeRangeBuckets = new ArrayList<>();

        for (Instant bucket = rangeStart;
             bucket.isBefore(rangeEnd);
             bucket = toNextRangeStart(bucket)) {

            Instant bucketStartTime = toRangeStart(bucket);
            Instant bucketEndTime = toRangeEnd(bucket);

            ChartGenerator subChartGenerator = subChartGeneratorSupplier.provideGenerator(
                    chartDefinition,
                    bucketStartTime,
                    bucketEndTime,
                    activities,
                    tags
            );
            DashboardChartData subChart = subChartGenerator.generate();
            ChartBucketData timeBucket = new ChartBucketData(
                    null,
                    bucketStartTime,
                    bucketEndTime,
                    bucketType(),
                    null,
                    null,
                    subChart.buckets()
            );
            timeRangeBuckets.add(timeBucket);
        }
        return new DashboardChartData(chartDefinition.name(), timeRangeBuckets);
    }

    protected abstract BucketType bucketType();

    protected abstract Instant toRangeStart(Instant timeInRange);

    protected abstract Instant toRangeEnd(Instant timeInRange);

    protected abstract Instant toNextRangeStart(Instant timeInRange);
}
