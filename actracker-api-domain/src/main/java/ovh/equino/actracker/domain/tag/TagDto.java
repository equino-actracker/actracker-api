package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.metric.Metric;

import java.util.Collection;
import java.util.UUID;

public record TagDto(

        UUID id,
        UUID creatorId,
        String name,
        Collection<Metric> metrics,
        boolean deleted

) {

    // Constructor for data provided from input
    public TagDto(String name, Collection<Metric> metrics) {
        this(null, null, name, metrics, false);
    }

}
