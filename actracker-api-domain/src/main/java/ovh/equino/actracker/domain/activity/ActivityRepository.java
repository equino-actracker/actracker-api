package ovh.equino.actracker.domain.activity;

import java.util.Optional;

public interface ActivityRepository {

    Optional<Activity> get(ActivityId activityId);

    void add(Activity activity);

    // TODO remove, replace with domain events
    void save(Activity activity);
}
