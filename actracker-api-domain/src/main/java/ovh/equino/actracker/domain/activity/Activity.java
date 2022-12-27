package ovh.equino.actracker.domain.activity;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;

class Activity {

    private UUID id;
    private Instant startTime;
    private Instant endTime;

    UUID getId() {
        return id;
    }

    static Activity newFrom(ActivityDto activity) {
        Activity newActivity = new Activity();
        newActivity.id = randomUUID();
        newActivity.startTime = activity.startTime();
        newActivity.endTime = activity.endTime();
        return newActivity;
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
    }

    ActivityDto toDto() {
        return new ActivityDto(id, startTime, endTime);
    }
}
