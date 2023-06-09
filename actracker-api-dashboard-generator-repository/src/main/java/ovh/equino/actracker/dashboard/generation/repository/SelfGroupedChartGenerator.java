package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.DashboardChartData;
import ovh.equino.actracker.domain.tag.TagDto;

import java.time.Instant;
import java.util.Collection;

class SelfGroupedChartGenerator extends ChartGenerator {

    private final ChartGeneratorSupplier chartGeneratorSupplier;

    SelfGroupedChartGenerator(Chart chartDefinition,
                              Instant rangeStart,
                              Instant rangeEnd,
                              Collection<ActivityDto> activities,
                              Collection<TagDto> tags,
                              ChartGeneratorSupplier chartGeneratorSupplier) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags);
        this.chartGeneratorSupplier = chartGeneratorSupplier;
    }

    @Override
    DashboardChartData generate() {
        ChartGenerator generator = this.chartGeneratorSupplier.provideGenerator(
                chartDefinition, rangeStart, rangeEnd, activities, tags
        );
        return generator.generate();
    }
}
