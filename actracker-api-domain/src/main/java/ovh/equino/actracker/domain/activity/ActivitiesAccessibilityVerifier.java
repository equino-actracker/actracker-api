package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.user.User;

public class ActivitiesAccessibilityVerifier {

    private final ActivityDataSource activityDataSource;
    private final User user;

    public ActivitiesAccessibilityVerifier(ActivityDataSource activityDataSource, User user) {
        this.activityDataSource = activityDataSource;
        this.user = user;
    }

    boolean isAccessible(ActivityId activityId) {
        return activityDataSource.find(activityId, user).isPresent();
    }
}
