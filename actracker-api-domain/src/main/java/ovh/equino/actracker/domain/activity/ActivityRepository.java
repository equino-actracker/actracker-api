package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {

    void add(ActivityDto activity);

    void update(UUID activityId, ActivityDto activity);

    // TODO delete when data sources proven
    Optional<ActivityDto> findById(UUID activityId);

    // TODO delete when data sources proven
    List<ActivityDto> find(EntitySearchCriteria searchCriteria);
}
