package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.user.User;

class ActivitiesAccessibilityVerifierImpl implements ActivitiesAccessibilityVerifier {

    private final ActivityDataSource activityDataSource;

    ActivitiesAccessibilityVerifierImpl(ActivityDataSource activityDataSource) {
        this.activityDataSource = activityDataSource;
    }

    @Override
    public boolean isAccessibleFor(User user, ActivityId activityId) {
        return activityDataSource.find(activityId, user).isPresent();
    }
}
