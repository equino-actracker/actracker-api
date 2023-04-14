package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {

    void add(ActivityDto activity);

    void update(UUID activityId, ActivityDto activity);

    Optional<ActivityDto> findById(UUID activityId);

    List<ActivityDto> find(EntitySearchCriteria searchCriteria);

    // TODO delete
    List<ActivityDto> findAll(User searcher);
}
