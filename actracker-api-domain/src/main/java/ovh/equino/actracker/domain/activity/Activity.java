package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private Instant startTime;
    private Instant endTime;

    Activity(
            ActivityId newId,
            ActivityDto activityData,
            User creator) {

        this.id = requireNonNull(newId);
        this.creator = requireNonNull(creator);
        this.startTime = activityData.startTime();
        this.endTime = activityData.endTime();
        validate();
    }

    Activity(ActivityDto activityData) {
        this(
                new ActivityId(activityData.id()),
                activityData,
                new User(activityData.creatorId())
        );
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
        return new ActivityDto(id.id(), creator.id(), startTime, endTime);
    }

    Instant startTime() {
        return startTime;
    }

    Instant endTime() {
        return endTime;
    }

}
