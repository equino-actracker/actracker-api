package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

public record TagDto(

        UUID id,
        UUID creatorId,
        String name,
        Collection<MetricDto> metrics,
        List<Share> shares,
        boolean deleted

) {

    // Constructor for data provided from input
    public TagDto(String name, Collection<MetricDto> metrics) {
        this(null, null, name, metrics, emptyList(), false);
    }

}
