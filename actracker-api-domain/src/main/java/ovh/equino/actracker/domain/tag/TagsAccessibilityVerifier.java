package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.Set;

public interface TagsAccessibilityVerifier {
    boolean isAccessibleFor(User user, TagId tag);

    Set<TagId> nonAccessibleFor(User user, Collection<TagId> tags);
}
