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

    static Activity createdFrom(ActivityDto activity) {
        Activity createdActivity = new Activity();
        createdActivity.id = randomUUID();
        createdActivity.startTime = activity.startTime();
        createdActivity.endTime = activity.endTime();
        return createdActivity;
    }

    static Activity existingFrom(ActivityDto activity) {
        Activity existingActivity = new Activity();
        existingActivity.id = activity.id();
        existingActivity.startTime = activity.startTime();
        existingActivity.endTime = activity.endTime();
        return existingActivity;
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
    }

    ActivityDto toDto() {
        return new ActivityDto(id, startTime, endTime);
    }
}
