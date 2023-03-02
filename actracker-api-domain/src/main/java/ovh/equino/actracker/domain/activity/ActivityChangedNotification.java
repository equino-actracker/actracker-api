package ovh.equino.actracker.domain.activity;

import java.util.UUID;

public record ActivityChangedNotification(
        long version,
        ActivityDto activity
) {

    ActivityChangedNotification(ActivityDto activityDto) {
        this(
                0L,
                activityDto
        );
    }

    public UUID id() {
        return activity.id();
    }
}
