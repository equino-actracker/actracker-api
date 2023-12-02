package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.user.User;

public class TagSetsAccessibilityVerifier {

    private final TagSetDataSource tagSetDataSource;
    private final User user;

    public TagSetsAccessibilityVerifier(TagSetDataSource tagSetDataSource, User user) {
        this.tagSetDataSource = tagSetDataSource;
        this.user = user;
    }

    boolean isAccessible(TagSetId tagSetId) {
        return tagSetDataSource.find(tagSetId, user).isPresent();
    }
}
