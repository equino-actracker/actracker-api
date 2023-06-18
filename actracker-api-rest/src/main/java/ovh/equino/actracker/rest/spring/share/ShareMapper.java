package ovh.equino.actracker.rest.spring.share;

import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

public class ShareMapper extends PayloadMapper {

    public ovh.equino.actracker.domain.share.Share fromRequest(Share share) {
        return new ovh.equino.actracker.domain.share.Share(share.granteeName());
    }

    public List<Share> toResponse(Collection<ovh.equino.actracker.domain.share.Share> shares) {
        return requireNonNullElse(shares, new ArrayList<ovh.equino.actracker.domain.share.Share>())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    Share toResponse(ovh.equino.actracker.domain.share.Share share) {
        return new Share(share.granteeName());
    }
}
