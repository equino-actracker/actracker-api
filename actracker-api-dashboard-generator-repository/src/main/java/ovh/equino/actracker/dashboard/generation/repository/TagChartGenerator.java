package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.BucketType;
import ovh.equino.actracker.domain.dashboard.generation.ChartBucketData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardChartData;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class TagChartGenerator extends ChartGenerator {

    private final PercentageCalculator percentageCalculator = new PercentageCalculator();

    TagChartGenerator(Chart chartDefinition,
                      Instant rangeStart,
                      Instant rangeEnd,
                      Collection<ActivityDto> activities,
                      Collection<TagDto> tags) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags);
    }

    @Override
    DashboardChartData generate() {
        Map<TagId, Duration> durationByTag = tags.stream()
                .map(TagDto::id)
                .map(TagId::new)
                .collect(toMap(
                        identity(),
                        tag -> totalDurationOf(activitiesWithTag(tag, activities))
                ));

        return new DashboardChartData(chartDefinition.name(), toBuckets(durationByTag));
    }

    Duration totalDurationOf(Collection<ActivityDto> activities) {
        return activities.stream()
                .map(activity -> Duration.between(activity.startTime(), activity.endTime()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    Collection<ActivityDto> activitiesWithTag(TagId tag, Collection<ActivityDto> activities) {
        return activities.stream()
                .filter(activityDto -> activityDto.tags().contains(tag.id()))
                .toList();
    }

    private List<ChartBucketData> toBuckets(Map<TagId, Duration> durationByTag) {
        Duration totalMeasuredDuration = durationByTag.values().stream()
                .reduce(Duration.ZERO, Duration::plus);

        return durationByTag.entrySet().stream()
                .map(entry -> toBucket(entry.getKey(), entry.getValue(), totalMeasuredDuration))
                .toList();
    }

    private ChartBucketData toBucket(TagId tagId, Duration tagDuration, Duration totalMeasuredDuration) {
        BigDecimal tagDurationSeconds = BigDecimal.valueOf(tagDuration.toSeconds());
        BigDecimal totalDurationSeconds = BigDecimal.valueOf(totalMeasuredDuration.toSeconds());
        return new ChartBucketData(
                tagId.id().toString(),
                null,
                null,
                BucketType.TAG,
                percentageCalculator.percentage(tagDurationSeconds, totalDurationSeconds),
                percentageCalculator.percentage(tagDurationSeconds, totalDurationSeconds),
                null
        );
    }
}
