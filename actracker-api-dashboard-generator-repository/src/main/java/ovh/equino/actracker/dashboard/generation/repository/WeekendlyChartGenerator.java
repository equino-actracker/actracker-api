package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.BucketType;
import ovh.equino.actracker.domain.tag.TagId;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.startOfDay;

class WeekendlyChartGenerator extends TimeChartGenerator {

    public WeekendlyChartGenerator(Chart chartDefinition,
                                   Instant rangeStart,
                                   Instant rangeEnd,
                                   Collection<ActivityDto> activities,
                                   Collection<TagId> tags,
                                   ChartGeneratorSupplier subChartGeneratorSupplier) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags, subChartGeneratorSupplier);
    }

    @Override
    protected BucketType bucketType() {
        return BucketType.WEEKEND;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return ZonedDateTime.ofInstant(timeInRange, UTC)
                .with(previousOrSame(FRIDAY))
                .with(HOUR_OF_DAY, 18)
                .toInstant();
    }

    @Override
    protected Instant toRangeEnd(Instant timeInRange) {
        Instant rangeStart = toRangeStart(timeInRange);
        return startOfDay(
                ZonedDateTime.ofInstant(rangeStart, UTC)
                        .with(next(MONDAY))
                        .toInstant()
        );
    }

    @Override
    protected Instant toNextRangeStart(Instant timeInRange) {
        return ZonedDateTime.ofInstant(timeInRange, UTC)
                .with(next(FRIDAY))
                .with(HOUR_OF_DAY, 18)
                .toInstant();
    }
}
