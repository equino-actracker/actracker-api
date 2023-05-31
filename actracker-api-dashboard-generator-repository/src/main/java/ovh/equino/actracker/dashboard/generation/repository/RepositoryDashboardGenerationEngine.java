package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
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

        Optional<Instant> rangeStartTime = rangeStartTime(activities, generationCriteria);
        if (rangeStartTime.isEmpty()) {
            return empty(dashboard);
        }

        Optional<Instant> rangeEndTime = rangeEndTime(activities, generationCriteria);
        if (rangeEndTime.isEmpty()) {
            return empty(dashboard);
        }

        List<DashboardChartData> chartsData = dashboard.charts()
                .stream()
                .map(chart -> generate(chart, rangeStartTime.get(), rangeEndTime.get(), tags, activities))
                .toList();

        return new DashboardData(dashboard.name(), chartsData);
    }

    private Optional<Instant> rangeStartTime(List<ActivityDto> activities,
                                             DashboardGenerationCriteria generationCriteria) {

        Instant activityEarliestStartTime = activities.stream()
                .map(ActivityDto::startTime)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
        if (activityEarliestStartTime == null) {
            return Optional.empty();
        }
        return Optional.of(
                latestOf(
                        startOfDay(activityEarliestStartTime),
                        generationCriteria.timeRangeStart()
                )
        );
    }

    private Optional<Instant> rangeEndTime(List<ActivityDto> activities,
                                           DashboardGenerationCriteria generationCriteria) {

        Instant activityLatestEndTime = activities.stream()
                .map(ActivityDto::endTime)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
        return Optional.ofNullable(
                earliestOf(
                        endOfDay(activityLatestEndTime),
                        generationCriteria.timeRangeEnd()
                )
        );
    }

    private DashboardChartData generate(Chart chart,
                                        Instant rangeStart,
                                        Instant rangeEnd,
                                        List<TagDto> tags,
                                        List<ActivityDto> activities) {

        Set<TagId> tagIds = tags.stream()
                .map(TagDto::id)
                .map(TagId::new)
                .collect(toUnmodifiableSet());

        ChartGenerator generator = switch (chart.groupBy()) {
            case TAG -> new TagChartGenerator(chart, rangeStart, rangeEnd, activities, tagIds);
            case DAY ->
                    new DailyChartGenerator(chart, rangeStart, rangeEnd, activities, tagIds, TagChartGenerator::new);
            case WEEK ->
                    new WeeklyChartGenerator(chart, rangeStart, rangeEnd, activities, tagIds, TagChartGenerator::new);
            case MONTH ->
                    new MonthlyChartGenerator(chart, rangeStart, rangeEnd, activities, tagIds, TagChartGenerator::new);
            case WEEKEND ->
                    new WeekendlyChartGenerator(chart, rangeStart, rangeEnd, activities, tagIds, TagChartGenerator::new);
        };

        return generator.generate();
    }

    DashboardData empty(DashboardDto dashboard) {
        List<DashboardChartData> emptyCharts = dashboard.charts()
                .stream()
                .map(chart -> new DashboardChartData(chart.name(), emptyList()))
                .toList();
        return new DashboardData(dashboard.name(), emptyCharts);
    }
}
