package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntityValidator;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class ActivityValidator extends EntityValidator<Activity> {

    @Override
    protected Class<Activity> entityType() {
        return Activity.class;
    }

    @Override
    protected List<String> collectValidationErrors(Activity activity) {
        List<String> validationErrors = new LinkedList<>();

        checkEndTimeBeforeStartTime(activity).ifPresent(validationErrors::add);

        return validationErrors;
    }

    private Optional<String> checkEndTimeBeforeStartTime(Activity activity) {
        if (endTimeBeforeStartTime(activity)) {
            return Optional.of("End time is before start time");
        }
        return Optional.empty();
    }

    private boolean endTimeBeforeStartTime(Activity activity) {
        Instant activityStartTime = activity.startTime();
        Instant activityEndTime = activity.endTime();

        if (activityStartTime == null || activityEndTime == null) {
            return false;
        }
        return activityEndTime.isBefore(activityStartTime);
    }
}
