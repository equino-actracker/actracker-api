package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityNotifier activityNotifier;

    ActivityServiceImpl(ActivityRepository activityRepository, ActivityNotifier activityNotifier) {
        this.activityRepository = activityRepository;
        this.activityNotifier = activityNotifier;
    }

    @Override
    public ActivityDto createActivity(ActivityDto newActivityData, User creator) {
        Activity activity = Activity.create(newActivityData, creator);
        activityRepository.add(activity.forStorage());
        activityNotifier.notifyChanged(activity.forChangeNotification());
        return activity.forClient();
    }

    @Override
    public ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater) {
        Activity activity = getActivityIfAuthorized(updater, activityId);
        activity.updateTo(updatedActivityData);
        activityRepository.update(activityId, activity.forStorage());
        activityNotifier.notifyChanged(activity.forChangeNotification());
        return activity.forClient();
    }

    @Override
    public List<ActivityDto> getActivities(User searcher) {
        List<Activity> activities = activityRepository.findAll(searcher).stream()
                .map(Activity::fromStorage)
                .toList();
        return activities.stream()
                .map(Activity::forClient)
                .toList();
    }

    @Override
    public void deleteActivity(UUID activityId, User remover) {
        Activity activity = getActivityIfAuthorized(remover, activityId);
        activity.delete();
        activityRepository.update(activityId, activity.forStorage());
        activityNotifier.notifyChanged(activity.forChangeNotification());
    }

    private Activity getActivityIfAuthorized(User user, UUID activityId) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto);

        if (activity.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(Activity.class, activityId);
        }
        return activity;
    }
}
