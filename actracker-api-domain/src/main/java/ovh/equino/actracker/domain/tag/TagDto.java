package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagDto(

        UUID id,
        UUID creatorId,
        String name,
        Collection<MetricDto> metrics,
        List<Share> shares,
        boolean deleted

) {

    public TagDto {
        metrics = requireNonNullElse(metrics, emptyList());
        shares = requireNonNullElse(shares, emptyList());
    }

    // TODO Get rid of!
    // Constructor for data provided from input
    public TagDto(String name, Collection<MetricDto> metrics, List<Share> shares) {
        this(null, null, name, metrics, shares, false);
    }

}
