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

        //@formatter:off
        List<TagDto> chartTags = chart.includesAllTags()
                ? allTags
                : allTags.stream()
                    .filter(tag -> chart.includedTags().contains(tag.id()))
                    .toList();
        //@formatter:on

        List<ActivityDto> matchingAlignedActivities = alignedTo(generationCriteria, allActivities);
        Set<TagId> allowedTags = chartTags.stream()
                .map(TagDto::id)
                .map(TagId::new)
                .collect(toUnmodifiableSet());
        Set<TagId> allTag = allTags.stream()
                .map(TagDto::id)
                .map(TagId::new)
                .collect(toUnmodifiableSet());

        ChartGenerator generator = switch (chart.groupBy()) {
            case TAG -> new TagChartGenerator(chart.name(), allowedTags, allTag);
            case DAY -> new DailyChartGenerator(new TagChartGenerator(chart.name(), allowedTags, allTag));
            case WEEK -> new WeeklyChartGenerator(new TagChartGenerator(chart.name(), allowedTags, allTag));
            case MONTH -> new MonthlyChartGenerator(new TagChartGenerator(chart.name(), allowedTags, allTag));
            case WEEKEND -> new WeekendlyChartGenerator(new TagChartGenerator(chart.name(), allowedTags, allTag));
        };

        return generator.generate(matchingAlignedActivities);
    }

    DashboardData empty(DashboardDto dashboard) {
        List<DashboardChartData> emptyCharts = dashboard.charts()
                .stream()
                .map(chart -> new DashboardChartData(chart.name(), emptyList()))
                .toList();
        return new DashboardData(dashboard.name(), emptyCharts);
    }

    // This is a code duplication (TimeChartGenerator)
    private List<ActivityDto> alignedTo(DashboardGenerationCriteria generationCriteria, List<ActivityDto> activities) {
        return activities.stream()
                .filter(activity -> !activity.startTime().isAfter(generationCriteria.timeRangeEnd()))
                .filter(activity -> activity.endTime() == null || !activity.endTime().isBefore(generationCriteria.timeRangeStart()))
                .map(activity -> new ActivityDto(
                        activity.title(),
                        latestOf(activity.startTime(), generationCriteria.timeRangeStart()),
                        earliestOf(activity.endTime(), generationCriteria.timeRangeEnd()),
                        activity.comment(),
                        activity.tags(),
                        activity.metricValues()
                ))
                .toList();
    }
}
