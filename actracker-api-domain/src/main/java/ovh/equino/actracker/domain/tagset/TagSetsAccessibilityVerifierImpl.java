package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.user.User;

class TagSetsAccessibilityVerifierImpl implements TagSetsAccessibilityVerifier {

    private final TagSetDataSource tagSetDataSource;

    TagSetsAccessibilityVerifierImpl(TagSetDataSource tagSetDataSource) {
        this.tagSetDataSource = tagSetDataSource;
    }

    @Override
    public boolean isAccessibleFor(User user, TagSetId tagSetId) {
        return tagSetDataSource.find(tagSetId, user).isPresent();
    }
}
