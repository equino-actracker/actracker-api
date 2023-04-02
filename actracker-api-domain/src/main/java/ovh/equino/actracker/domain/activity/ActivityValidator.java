package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntityValidator;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

final class ActivityValidator extends EntityValidator<Activity> {

    private final Activity activity;

    ActivityValidator(Activity activity) {
        this.activity = activity;
    }

    void validate() {
        List<String> validationErrors = new LinkedList<>();

        if (endTimeBeforeStartTime()) {
            validationErrors.add("End time is before start time");
        }

        handleValidationErrors(validationErrors);
    }

    private boolean endTimeBeforeStartTime() {
        Instant activityStartTime = activity.startTime();
        Instant activityEndTime = activity.endTime();

        if (activityStartTime == null || activityEndTime == null) {
            return false;
        }
        return activityEndTime.isBefore(activityStartTime);
    }

    @Override
    public Class<Activity> entityType() {
        return Activity.class;
    }
}
