package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
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

    // TODO delete when data sources proven
    List<ActivityDto> findUnfinishedStartedBefore(Instant startTime, User user);

}
