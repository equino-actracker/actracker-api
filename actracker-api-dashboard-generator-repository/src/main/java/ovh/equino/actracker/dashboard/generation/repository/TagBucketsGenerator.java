package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.ChartBucketData;
import ovh.equino.actracker.domain.tag.TagDto;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static ovh.equino.actracker.domain.dashboard.ChartBucketData.BucketType.TAG;

class TagBucketsGenerator {

    List<ChartBucketData> generate(List<TagDto> tags, List<ActivityDto> activities) {
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
