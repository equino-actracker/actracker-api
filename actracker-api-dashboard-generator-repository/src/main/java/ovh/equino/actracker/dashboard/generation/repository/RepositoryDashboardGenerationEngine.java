package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.*;

class RepositoryDashboardGenerationEngine implements DashboardGenerationEngine {

    private final TagFinder tagFinder;
    private final ActivityFinder activityFinder;

    RepositoryDashboardGenerationEngine(TagSearchEngine tagSearchEngine, ActivitySearchEngine activitySearchEngine) {
        this.tagFinder = new TagFinder(tagSearchEngine);
        this.activityFinder = new ActivityFinder(activitySearchEngine);
    }

    @Override
    public DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationCriteria generationCriteria) {

        List<TagDto> tags = tagFinder.find(generationCriteria);
        if (isEmpty(tags)) {
            return empty(dashboard);
        }

        List<ActivityDto> activities = activityFinder.find(generationCriteria);

        Instant earliestStartTime = activities.stream()
                .map(ActivityDto::startTime)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
        if (earliestStartTime == null) {
            return empty(dashboard);
        }
        earliestStartTime = latestOf(beginningOfDay(earliestStartTime), generationCriteria.timeRangeStart());

        Instant latestEndTime = activities.stream()
                .map(ActivityDto::endTime)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
        latestEndTime = earliestOf(endOfDay(latestEndTime), generationCriteria.timeRangeEnd());
        if (latestEndTime == null) {
            return empty(dashboard);
        }

        DashboardGenerationCriteria dataDrivenGenerationCriteria = new DashboardGenerationCriteria(
                generationCriteria.dashboardId(),
                generationCriteria.generator(),
                earliestStartTime,
                latestEndTime,
                generationCriteria.tags()
        );

        List<DashboardChartData> chartsData = dashboard.charts().stream()
                .map(chart -> generate(chart, dataDrivenGenerationCriteria, tags, activities))
                .toList();

        return new DashboardData(dashboard.name(), chartsData);
    }

    private DashboardChartData generate(Chart chart,
                                        DashboardGenerationCriteria generationCriteria,
                                        List<TagDto> allTags,
                                        List<ActivityDto> allActivities) {

        return switch (chart.groupBy()) {
            case TAG -> generateChartGroupedByTag(chart.name(), generationCriteria, allTags, allActivities);
            case DAY -> generateChartGroupedByDay(chart.name(), generationCriteria, allTags, allActivities);
        };
    }

    private DashboardChartData generateChartGroupedByTag(String chartName,
                                                         DashboardGenerationCriteria generationCriteria,
                                                         List<TagDto> allTags,
                                                         List<ActivityDto> allActivities) {

        List<ActivityDto> matchingAlignedActivities = alignedTo(generationCriteria, allActivities);

        List<ChartBucketData> buckets = new TagBucketsGenerator().generate(allTags, matchingAlignedActivities);

        return new DashboardChartData(chartName, buckets);
    }

    private DashboardChartData generateChartGroupedByDay(String chartName,
                                                         DashboardGenerationCriteria generationCriteria,
                                                         List<TagDto> allTags,
                                                         List<ActivityDto> allActivities) {

        List<ChartBucketData> dailyBuckets = new ArrayList<>();

        for (Instant bucket = generationCriteria.timeRangeStart();
             bucket.isBefore(generationCriteria.timeRangeEnd());
             bucket = bucket.plus(1, DAYS)) {

            DashboardGenerationCriteria forDayGenerationCriteria = new DashboardGenerationCriteria(
                    generationCriteria.dashboardId(),
                    generationCriteria.generator(),
                    bucket,
                    bucket.plus(1, DAYS).minusMillis(1),
                    generationCriteria.tags()
            );

            List<ActivityDto> matchingAlignedActivities = alignedTo(forDayGenerationCriteria, allActivities);

            DashboardChartData chartByTag = generateChartGroupedByTag(
                    Long.toString(bucket.toEpochMilli()),
                    forDayGenerationCriteria,
                    allTags,
                    matchingAlignedActivities
            );

            ChartBucketData dailyBucket = new ChartBucketData(
                    Long.toString(bucket.toEpochMilli()),
                    ChartBucketData.BucketType.DAY,
                    null,
                    null,
                    chartByTag.buckets()
            );
            dailyBuckets.add(dailyBucket);
        }

        return new DashboardChartData(chartName, dailyBuckets);
    }

    DashboardData empty(DashboardDto dashboard) {
        List<DashboardChartData> emptyCharts = dashboard.charts()
                .stream()
                .map(chart -> new DashboardChartData(chart.name(), emptyList()))
                .toList();
        return new DashboardData(dashboard.name(), emptyCharts);
    }

    private List<ActivityDto> alignedTo(DashboardGenerationCriteria generationCriteria, List<ActivityDto> activities) {
        return activities.stream()
                .filter(activity -> !activity.startTime().isAfter(generationCriteria.timeRangeEnd()))
                .filter(activity -> activity.endTime() == null || !activity.endTime().isBefore(generationCriteria.timeRangeStart()))
                .map(activity -> new ActivityDto(
                        activity.title(),
                        latestOf(activity.startTime(), generationCriteria.timeRangeStart()),
                        earliestOf(activity.endTime(), generationCriteria.timeRangeEnd()),
                        activity.comment(),
                        activity.tags()
                ))
                .toList();
    }
}
