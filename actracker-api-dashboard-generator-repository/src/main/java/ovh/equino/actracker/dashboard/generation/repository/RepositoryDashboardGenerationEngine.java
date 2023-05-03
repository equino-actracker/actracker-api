package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static ovh.equino.actracker.domain.dashboard.ChartBucketData.BucketType.TAG;

class RepositoryDashboardGenerationEngine implements DashboardGenerationEngine {

    private static final Integer PAGE_SIZE = 500;

    private final TagSearchEngine tagSearchEngine;
    private final ActivitySearchEngine activitySearchEngine;

    RepositoryDashboardGenerationEngine(TagSearchEngine tagSearchEngine, ActivitySearchEngine activitySearchEngine) {
        this.tagSearchEngine = tagSearchEngine;
        this.activitySearchEngine = activitySearchEngine;
    }

    @Override
    public DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationCriteria generationCriteria) {
        List<DashboardChartData> chartsData = dashboard.charts().stream()
                .map(chart -> generate(chart, generationCriteria))
                .toList();

        return new DashboardData(dashboard.name(), chartsData);
    }

    private DashboardChartData generate(Chart chart, DashboardGenerationCriteria generationCriteria) {
        return generateChartGroupedByTag(chart.name(), generationCriteria);
//        return switch (chart.groupBy()) {
//            case TAG -> dashboardRepository.generateChartGroupedByTags(chart.name(), generationCriteria);
//            case DAY -> dashboardRepository.generateChartGroupedByDays(chart.name(), generationCriteria);
//        };
    }

    private DashboardChartData generateChartGroupedByTag(String chartName,
                                                         DashboardGenerationCriteria generationCriteria) {

        List<TagDto> tags = findAllTags(generationCriteria);
        if (isEmpty(tags)) {
            new DashboardChartData(chartName, emptyList());
        }

        List<ActivityDto> allActivities = findAllActivities(generationCriteria);
        List<ActivityDto> activities = allActivities.stream()
                .filter(activity -> isNotEmpty(activity.tags()))
                .toList();
        if (isEmpty(activities)) {
            new DashboardChartData(chartName, emptyList());
        }

        List<ChartBucketData> buckets = tagBuckets(tags, activities);

        return new DashboardChartData(chartName, buckets);
    }

    private List<ChartBucketData> tagBuckets(List<TagDto> tags, List<ActivityDto> activities) {
        Map<TagDto, Duration> durationByTag = tags.stream()
                .collect(toMap(identity(), tag -> totalDurationOf(activitiesWithTag(tag, activities))));
        Duration totalMeasuredDuration = durationByTag.values().stream()
                .reduce(Duration.ZERO, Duration::plus);


        return totalMeasuredDuration.isZero()
                ? toEmptyBuckets(tags)
                : toBuckets(durationByTag, totalMeasuredDuration);
    }

    private List<ChartBucketData> toBuckets(Map<TagDto, Duration> durationByTag, Duration totalMeasuredDuration) {
        return durationByTag.entrySet().stream()
                .map(entry -> {
                    TagDto tag = entry.getKey();
                    BigDecimal tagDuration = BigDecimal.valueOf(entry.getValue().toSeconds());
                    BigDecimal totalDuration = BigDecimal.valueOf(totalMeasuredDuration.toSeconds());
                    return new ChartBucketData(
                            tag.id().toString(),
                            TAG,
                            tagDuration,
                            tagDuration.divide(totalDuration, 4, HALF_UP),
                            null
                    );
                })
                .toList();
    }

    private List<ChartBucketData> toEmptyBuckets(List<TagDto> tags) {
        return tags.stream()
                .map(tag -> new ChartBucketData(
                        tag.id().toString(),
                        TAG,
                        null,
                        null,
                        null
                ))
                .toList();
    }

    private List<ActivityDto> findAllActivities(DashboardGenerationCriteria generationCriteria) {
        List<ActivityDto> activities = new ArrayList<>();
        String pageId = "";
        while (pageId != null) {
            EntitySearchResult<ActivityDto> searchResult = fetchNextPageOfActivities(generationCriteria, pageId);
            pageId = searchResult.nextPageId();
            activities.addAll(alignedToTimeRange(searchResult.results(), generationCriteria));
        }
        return activities;
    }

    private List<TagDto> findAllTags(DashboardGenerationCriteria generationCriteria) {
        List<TagDto> tags = new ArrayList<>();
        String pageId = "";
        while (pageId != null) {
            EntitySearchResult<TagDto> searchResult = fetchNextPageOfTags(generationCriteria, pageId);
            pageId = searchResult.nextPageId();
            tags.addAll(searchResult.results());
        }
        return tags;
    }

    private Collection<ActivityDto> alignedToTimeRange(List<ActivityDto> activities, DashboardGenerationCriteria generationCriteria) {
        return activities.stream()
                .map(activity ->
                        new ActivityDto(
                                activity.title(),
                                latestOf(generationCriteria.timeRangeStart(), activity.startTime()),
                                earliestOf(generationCriteria.timeRangeEnd(), activity.endTime()),
                                activity.comment(),
                                activity.tags()
                        )
                )
                .filter(activity -> nonNull(activity.startTime()) && nonNull(activity.endTime()))
                .toList();
    }

    private EntitySearchResult<TagDto> fetchNextPageOfTags(DashboardGenerationCriteria generationCriteria,
                                                           String pageId) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                generationCriteria.generator(),
                PAGE_SIZE,
                pageId,
                null,
                null,
                null,
                null
        );
        return tagSearchEngine.findTags(searchCriteria);
    }

    private EntitySearchResult<ActivityDto> fetchNextPageOfActivities(DashboardGenerationCriteria generationCriteria,
                                                                      String pageId) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                generationCriteria.generator(),
                PAGE_SIZE,
                pageId,
                null,
                generationCriteria.timeRangeStart(),
                generationCriteria.timeRangeEnd(),
                null
        );
        return activitySearchEngine.findActivities(searchCriteria);
    }

    Instant earliestOf(Instant... candidates) {
        return stream(candidates)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
    }

    Instant latestOf(Instant... candidates) {
        return stream(candidates)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
    }

    Duration totalDurationOf(Collection<ActivityDto> activities) {
        return activities.stream()
                .map(activity -> Duration.between(activity.startTime(), activity.endTime()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    Collection<ActivityDto> activitiesWithTag(TagDto tag, Collection<ActivityDto> activities) {
        return activities.stream()
                .filter(activityDto -> activityDto.tags().contains(tag.id()))
                .toList();
    }

}
