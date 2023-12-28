package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.user.User;

public interface ActivitiesAccessibilityVerifier {
    boolean isAccessibleFor(User user, ActivityId activityId);
}
