package ovh.equino.actracker.domain.activity;

import java.util.UUID;

public record ActivityChangedNotification(
        ActivityDto activity
) {

    public UUID id() {
        return activity.id();
    }
}
