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
    private boolean deleted;

    Activity(
            ActivityId newId,
            ActivityDto activityData,
            User creator) {

        this.id = requireNonNull(newId);
        this.creator = requireNonNull(creator);
        this.startTime = activityData.startTime();
        this.endTime = activityData.endTime();
        this.deleted = false;
        validate();
    }

    static Activity fromDto(ActivityDto activityData) {
        Activity activity = new Activity(
                new ActivityId(activityData.id()),
                activityData,
                new User(activityData.creatorId())
        );
        activity.deleted = activityData.deleted();

        return activity;
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
        validate();
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    void delete() {
        this.deleted = true;
    }

    @Override
    public void validate() {
        new ActivityValidator(this).validate();
    }

    ActivityDto toDto() {
        return new ActivityDto(id.id(), creator.id(), startTime, endTime, deleted);
    }

    ActivityChangedNotification toChangeNotification() {
        return new ActivityChangedNotification(this.toDto());
    }

    Instant startTime() {
        return startTime;
    }

    Instant endTime() {
        return endTime;
    }

}
