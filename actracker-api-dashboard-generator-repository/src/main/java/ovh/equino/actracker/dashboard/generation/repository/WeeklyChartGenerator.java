package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.BucketType;
import ovh.equino.actracker.domain.tag.TagDto;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;

import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.startOfDay;

class WeeklyChartGenerator extends TimeChartGenerator {

    WeeklyChartGenerator(Chart chartDefinition,
                         Instant rangeStart,
                         Instant rangeEnd,
                         Collection<ActivityDto> activities,
                         Collection<TagDto> tags,
                         ChartGeneratorSupplier subChartGeneratorSupplier) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags, subChartGeneratorSupplier);
    }

    @Override
    protected BucketType bucketType() {
        return BucketType.WEEK;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return startOfDay(
                ZonedDateTime.ofInstant(timeInRange, UTC)
                        .with(previousOrSame(MONDAY))
                        .toInstant()
        );
    }

    @Override
    protected Instant toRangeEnd(Instant timeInRange) {
        return toNextRangeStart(timeInRange).minusMillis(1);
    }

    @Override
    protected Instant toNextRangeStart(Instant timeInRange) {
        return startOfDay(
                ZonedDateTime.ofInstant(timeInRange, UTC)
                        .with(next(MONDAY))
                        .toInstant()
        );
    }
}
