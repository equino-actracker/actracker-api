package ovh.equino.actracker.rest.spring.tag;

import ovh.equino.actracker.rest.spring.share.Share;

import java.util.List;

record Tag(
        String id,
        String name,
        List<Metric> metrics,
        List<Share> shares
) {
}
