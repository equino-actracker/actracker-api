package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.tag.TagId;

import java.time.Instant;
import java.util.Collection;

interface ChartGeneratorSupplier {

    ChartGenerator provideGenerator(Chart chartDefinition,
                                    Instant rangeStart,
                                    Instant rangeEnd,
                                    Collection<ActivityDto> activities,
                                    Collection<TagId> tags
    );
}
