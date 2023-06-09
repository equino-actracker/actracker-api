package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.DashboardChartData;
import ovh.equino.actracker.domain.tag.TagDto;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.earliestOf;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.latestOf;

abstract class ChartGenerator {

    protected final Chart chartDefinition;
    protected final Instant rangeStart;
    protected final Instant rangeEnd;
    protected final List<ActivityDto> activities;
    protected final Set<TagDto> tags;

    protected ChartGenerator(Chart chartDefinition,
                             Instant rangeStart,
                             Instant rangeEnd,
                             Collection<ActivityDto> activities,
                             Collection<TagDto> tags) {

        this.chartDefinition = chartDefinition;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.activities = alignedToRange(activities);

        //@formatter:off
        this.tags = chartDefinition.includesAllTags()
                ? tags.stream().collect(toUnmodifiableSet())
                : tags.stream()
                    .filter(tag -> chartDefinition.includedTags().contains(tag.id()))
                    .collect(toUnmodifiableSet());
        //@formatter:on
    }

    abstract DashboardChartData generate();

    private List<ActivityDto> alignedToRange(Collection<ActivityDto> activities) {
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
}
