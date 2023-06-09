package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.BucketType;
import ovh.equino.actracker.domain.tag.TagDto;

import java.time.Instant;
import java.util.Collection;

import static ovh.equino.actracker.dashboard.generation.repository.DashboardUtils.*;

class DailyChartGenerator extends TimeChartGenerator {

    DailyChartGenerator(Chart chartDefinition,
                        Instant rangeStart,
                        Instant rangeEnd,
                        Collection<ActivityDto> activities,
                        Collection<TagDto> tags,
                        ChartGeneratorSupplier subChartGeneratorSupplier) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags, subChartGeneratorSupplier);
    }

    @Override
    protected BucketType bucketType() {
        return BucketType.DAY;
    }

    @Override
    protected Instant toRangeStart(Instant timeInRange) {
        return startOfDay(timeInRange);
    }

    @Override
    protected Instant toRangeEnd(Instant timeInRange) {
        return endOfDay(timeInRange);
    }

    @Override
    protected Instant toNextRangeStart(Instant timeInRange) {
        return startOfNextDay(timeInRange);
    }
}
