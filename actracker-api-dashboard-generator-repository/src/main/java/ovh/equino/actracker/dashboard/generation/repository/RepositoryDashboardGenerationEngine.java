package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.generation.DashboardChartData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;
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
                .filter(not(Chart::isDeleted))
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

        ChartGeneratorSupplier subBucketsGenerator = switch (chart.analysisMetric()) {
            case TAG_PERCENTAGE -> TagChartGenerator::new;
            case TAG_DURATION -> TagChartGenerator::new;
            case METRIC_VALUE -> MetricValueChartGenerator::new;
        };

        ChartGenerator generator = switch (chart.groupBy()) {
            case SELF ->
                    new SelfGroupedChartGenerator(chart, rangeStart, rangeEnd, activities, tags, subBucketsGenerator);
            case DAY ->
                    new DailyChartGenerator(chart, rangeStart, rangeEnd, activities, tags, subBucketsGenerator);
            case WEEK ->
                    new WeeklyChartGenerator(chart, rangeStart, rangeEnd, activities, tags, subBucketsGenerator);
            case MONTH ->
                    new MonthlyChartGenerator(chart, rangeStart, rangeEnd, activities, tags, subBucketsGenerator);
            case WEEKEND ->
                    new WeekendlyChartGenerator(chart, rangeStart, rangeEnd, activities, tags, subBucketsGenerator);
        };

        return generator.generate();
    }

    DashboardData empty(DashboardDto dashboard) {
        List<DashboardChartData> emptyCharts = dashboard.charts()
                .stream()
                .filter(not(Chart::isDeleted))
                .map(chart -> new DashboardChartData(chart.name(), emptyList()))
                .toList();
        return new DashboardData(dashboard.name(), emptyCharts);
    }
}
