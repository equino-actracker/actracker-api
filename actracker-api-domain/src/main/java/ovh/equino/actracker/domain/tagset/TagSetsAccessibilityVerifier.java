package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.user.User;

public interface TagSetsAccessibilityVerifier {
    boolean isAccessibleFor(User user, TagSetId tagSetId);
}
