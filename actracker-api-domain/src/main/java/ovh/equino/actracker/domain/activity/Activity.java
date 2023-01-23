package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;

import java.time.Instant;

class Activity implements Entity {

    private final ActivityId id;
    private Instant startTime;
    private Instant endTime;

    Activity(ActivityId newId, ActivityDto activityData) {
        this.id = newId;
        this.startTime = activityData.startTime();
        this.endTime = activityData.endTime();
        validate();
    }

    Activity(ActivityDto activityData) {
        this(new ActivityId(activityData.id()), activityData);
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
        validate();
    }

    @Override
    public void validate() {
        new ActivityValidator(this).validate();
    }

    ActivityDto toDto() {
        return new ActivityDto(id.id(), startTime, endTime);
    }

    Instant getStartTime() {
        return startTime;
    }

    Instant getEndTime() {
        return endTime;
    }
}
