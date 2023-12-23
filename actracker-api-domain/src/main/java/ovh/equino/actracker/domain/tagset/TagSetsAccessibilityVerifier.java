package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.user.User;

public class TagSetsAccessibilityVerifier {

    private final TagSetDataSource tagSetDataSource;

    TagSetsAccessibilityVerifier(TagSetDataSource tagSetDataSource) {
        this.tagSetDataSource = tagSetDataSource;
    }

    boolean isAccessibleFor(User user, TagSetId tagSetId) {
        return tagSetDataSource.find(tagSetId, user).isPresent();
    }
}
