package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.function.Predicate.not;
import static java.util.stream.Stream.concat;

public class IntegrationTestActivitiesConfiguration {

    private final List<ActivityDto> addedActivities = new ArrayList<>();
    private final List<ActivityDto> transientActivities = new ArrayList<>();

    void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addActivities(addedActivities.toArray(new ActivityDto[0]));
    }

    public void add(ActivityDto activity) {
        addedActivities.add(activity);
    }

    public void addTransient(ActivityDto activity) {
        transientActivities.add(activity);
    }

    public List<ActivityDto> accessibleFor(User user) {
        return addedActivities
                .stream()
                .filter(not(ActivityDto::deleted))
//                .filter(activity -> isOwnerOrGrantee(user, activity))
//                .map(activity -> toAccessibleFormFor(user, activity))
//                .sorted(comparing(activity -> activity.id().toString()))
                .toList();
    }

    public List<ActivityDto> inaccessibleFor(User user) {
        List<UUID> accessibleActivities = accessibleFor(user)
                .stream()
                .map(ActivityDto::id)
                .toList();
        return concat(addedActivities.stream(), transientActivities.stream())
                .filter(activity -> !accessibleActivities.contains(activity.id()))
                .toList();
    }
}
