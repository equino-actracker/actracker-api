package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;

public interface TagFactory {
    Tag create(String name, Collection<Metric> metrics, Collection<Share> shares);

    Tag reconstitute(TagId id,
                     User creator,
                     String name,
                     Collection<Metric> metrics,
                     Collection<Share> shares,
                     boolean deleted);
}
