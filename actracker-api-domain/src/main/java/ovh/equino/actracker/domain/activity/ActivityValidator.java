package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.activity.error.ActivityInvalidException;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

final class ActivityValidator {

    private final Activity activity;
    private final List<String> validationErrors = new LinkedList<>();

    ActivityValidator(Activity activity) {
        this.activity = activity;
    }

    void validate() {
        if (endTimeBeforeStartTime()) {
            validationErrors.add("End time is before start time");
        }

        if (!validationErrors.isEmpty()) {
            throw new ActivityInvalidException(validationErrors);
        }
    }

    private boolean endTimeBeforeStartTime() {
        Instant activityStartTime = activity.startTime();
        Instant activityEndTime = activity.endTime();

        if (activityStartTime == null || activityEndTime == null) {
            return false;
        }
        return activityEndTime.isBefore(activityStartTime);
    }
}
