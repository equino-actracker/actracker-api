package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

class ShareMapper extends PayloadMapper {

    ovh.equino.actracker.domain.share.Share fromRequest(Share share) {
        return new ovh.equino.actracker.domain.share.Share(share.granteeName());
    }

    List<Share> toResponse(Collection<ovh.equino.actracker.domain.share.Share> shares) {
        return requireNonNullElse(shares, new ArrayList<ovh.equino.actracker.domain.share.Share>())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    Share toResponse(ovh.equino.actracker.domain.share.Share share) {
        return new Share(share.granteeName());
    }
}
