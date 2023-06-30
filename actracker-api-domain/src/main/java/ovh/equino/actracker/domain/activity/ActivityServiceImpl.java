package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivitySearchEngine activitySearchEngine;
    private final TagRepository tagRepository;
    private final ActivityNotifier activityNotifier;

    ActivityServiceImpl(ActivityRepository activityRepository,
                        ActivitySearchEngine activitySearchEngine,
                        TagRepository tagRepository,
                        ActivityNotifier activityNotifier) {

        this.activityRepository = activityRepository;
        this.activitySearchEngine = activitySearchEngine;
        this.tagRepository = tagRepository;
        this.activityNotifier = activityNotifier;
    }

    @Override
    public ActivityDto createActivity(ActivityDto newActivityData, User creator) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, creator);
        Activity activity = Activity.create(newActivityData, creator, tagsExistenceVerifier);
        activityRepository.add(activity.forStorage());
        activityNotifier.notifyChanged(activity.forChangeNotification());
        return activity.forClient(creator);
    }

    @Override
    public ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater) {
        Activity activity = getActivityIfAuthorized(updater, activityId);
        activity.updateTo(updatedActivityData);
        activityRepository.update(activityId, activity.forStorage());
        activityNotifier.notifyChanged(activity.forChangeNotification());
        return activity.forClient(updater);
    }

    @Override
    public EntitySearchResult<ActivityDto> searchActivities(EntitySearchCriteria searchCriteria) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, searchCriteria.searcher());

        EntitySearchResult<ActivityDto> searchResult = activitySearchEngine.findActivities(searchCriteria);
        List<ActivityDto> resultForClient = searchResult.results().stream()
                .map(activity -> Activity.fromStorage(activity, tagsExistenceVerifier))
                .map(activity -> activity.forClient(searchCriteria.searcher()))
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    @Override
    public void deleteActivity(UUID activityId, User remover) {
        Activity activity = getActivityIfAuthorized(remover, activityId);
        activity.delete(remover);
        activityRepository.update(activityId, activity.forStorage());
        activityNotifier.notifyChanged(activity.forChangeNotification());
    }

    @Override
    public ActivityDto switchToNewActivity(ActivityDto activityToSwitch, User switcher) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, switcher);
        Activity newActivity = Activity.create(activityToSwitch, switcher, tagsExistenceVerifier);
        Instant switchTime = newActivity.isStarted() ? newActivity.startTime() : now();
        newActivity.start(switchTime, switcher);

        List<Activity> activitiesToFinish = activityRepository.findUnfinishedStartedBefore(switchTime, switcher).stream()
                .map(activity -> Activity.fromStorage(activity, tagsExistenceVerifier))
                .toList();

        activitiesToFinish.forEach(activity -> activity.finish(switchTime, switcher));
        activitiesToFinish.stream()
                .map(Activity::forStorage)
                .forEach(activity -> activityRepository.update(activity.id(), activity));
        activitiesToFinish.stream()
                .map(Activity::forChangeNotification)
                .forEach(activityNotifier::notifyChanged);

        activityRepository.add(newActivity.forStorage());
        activityNotifier.notifyChanged(newActivity.forChangeNotification());

        return newActivity.forClient(switcher);
    }

    private Activity getActivityIfAuthorized(User user, UUID activityId) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, user);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier);

        if (activity.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(Activity.class, activityId);
        }
        return activity;
    }
}
