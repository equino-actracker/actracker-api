package ovh.equino.actracker.domain.activity;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {

    // TODO remove
    void add(ActivityDto activity);

    // TODO remove
    void update(UUID activityId, ActivityDto activity);

    // TODO remove
    Optional<ActivityDto> findById(UUID activityId);

//    Optional<Activity> get(ActivityId activityId);

//    void add(Activity activity);

    // TODO remove, replace with domain events
//    void save(Activity activity);
}
