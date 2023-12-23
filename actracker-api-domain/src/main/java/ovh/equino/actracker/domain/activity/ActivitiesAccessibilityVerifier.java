package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.user.User;

public class ActivitiesAccessibilityVerifier {

    private final ActivityDataSource activityDataSource;

    // TODO package-private
    public ActivitiesAccessibilityVerifier(ActivityDataSource activityDataSource) {
        this.activityDataSource = activityDataSource;
    }

    boolean isAccessibleFor(User user, ActivityId activityId) {
        return activityDataSource.find(activityId, user).isPresent();
    }
}
