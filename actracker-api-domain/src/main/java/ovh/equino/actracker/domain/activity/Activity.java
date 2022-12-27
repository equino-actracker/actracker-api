package ovh.equino.actracker.domain.activity;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;

class Activity {

    private UUID id;
    private Instant startTime;
    private Instant endTime;

    public Activity(ActivityDto activity) {
        id = randomUUID();
        startTime = activity.startTime();
        endTime = activity.endTime();
    }

    UUID getId() {
        return id;
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
    }

    ActivityDto toDto() {
        return new ActivityDto(id, startTime, endTime);
    }
}
