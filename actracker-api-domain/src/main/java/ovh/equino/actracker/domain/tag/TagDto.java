package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record TagDto(

        UUID id,
        UUID creatorId,
        String name,
        Collection<MetricDto> metrics,
        List<Share> shares,
        boolean deleted

) {

    public TagDto {
        requireNonNull(metrics);
        requireNonNull(shares);
    }

    // Constructor for data provided from input
    public TagDto(String name, Collection<MetricDto> metrics, List<Share> shares) {
        this(null, null, name, metrics, shares, false);
    }

}
