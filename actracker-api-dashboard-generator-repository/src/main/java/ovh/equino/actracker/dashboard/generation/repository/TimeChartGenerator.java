package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.ChartBucketData;
import ovh.equino.actracker.domain.dashboard.DashboardChartData;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.earliestOf;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.latestOf;

abstract class TimeChartGenerator extends ChartGenerator {

    protected final ChartGenerator subChartGenerator;

    protected TimeChartGenerator(ChartGenerator subChartGenerator) {
        super(subChartGenerator.chartName, subChartGenerator.tags, subChartGenerator.tags);
        this.subChartGenerator = subChartGenerator;
    }

    @Override
    DashboardChartData generate(Collection<ActivityDto> activities) {

        Instant timelineStartTime = activities.stream()
                .map(ActivityDto::startTime)
                .min(Instant::compareTo)
                .orElse(null);
        Instant timelineEndTime = activities.stream()
                .map(ActivityDto::endTime)
                .max(Instant::compareTo)
                .orElse(null);

        List<ChartBucketData> timeRangeBuckets = new ArrayList<>();

        for (Instant bucket = timelineStartTime;
             bucket.isBefore(timelineEndTime);
             bucket = toNextRangeStart(bucket)) {

            Instant bucketStartTime = toRangeStart(bucket);
            Instant bucketEndTime = toRangeEnd(bucket);

            List<ActivityDto> matchingAlignedActivities = alignedTo(bucketStartTime, bucketEndTime, activities);
            DashboardChartData subChart = subChartGenerator.generate(matchingAlignedActivities);
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
        return new DashboardChartData(chartName, timeRangeBuckets);
    }

    // This is a duplication (RepositoryDashboardGenerationEngine)
    private List<ActivityDto> alignedTo(Instant rangeStart, Instant rangeEnd, Collection<ActivityDto> activities) {
        return activities.stream()
                .filter(activity -> !activity.startTime().isAfter(rangeEnd))
                .filter(activity -> activity.endTime() == null || !activity.endTime().isBefore(rangeStart))
                .map(activity -> new ActivityDto(
                        activity.title(),
                        latestOf(activity.startTime(), rangeStart),
                        earliestOf(activity.endTime(), rangeEnd),
                        activity.comment(),
                        activity.tags(),
                        activity.metricValues()
                ))
                .toList();
    }

    protected abstract ChartBucketData.Type bucketType();

    protected abstract Instant toRangeStart(Instant timeInRange);

    protected abstract Instant toRangeEnd(Instant timeInRange);

    protected abstract Instant toNextRangeStart(Instant timeInRange);

    protected Instant toStartOfDay(Instant instant) {
        return ZonedDateTime.ofInstant(instant, UTC)
                .with(HOUR_OF_DAY, 0)
                .toInstant();
    }

    protected Instant toStartOfNextDay(Instant instant) {
        return toStartOfDay(instant)
                .plus(1, DAYS);
    }
}
