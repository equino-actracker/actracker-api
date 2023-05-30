package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.DashboardChartData;
import ovh.equino.actracker.domain.tag.TagId;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

abstract class ChartGenerator {

    protected final Set<TagId> tags;
    protected final String chartName;

    protected ChartGenerator(String chartName, Set<TagId> allowedTags, Set<TagId> availableTags) {
        this.chartName = chartName;
        if (isEmpty(allowedTags)) {
            this.tags = availableTags;
        } else {
            this.tags = availableTags.stream()
                    .filter(allowedTags::contains)
                    .collect(toUnmodifiableSet());
        }
    }

    abstract DashboardChartData generate(Collection<ActivityDto> activities);
}
