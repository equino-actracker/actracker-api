package ovh.equino.actracker.domain.activity;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {

    void add(ActivityDto activity);

    void update(UUID activityId, ActivityDto activity);

    // TODO delete when data sources proven
    Optional<ActivityDto> findById(UUID activityId);
}
