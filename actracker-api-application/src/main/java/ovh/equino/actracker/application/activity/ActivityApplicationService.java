package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public class ActivityApplicationService {

    private final ActivityRepository activityRepository;
    private final ActivityDataSource activityDataSource;
    private final ActivitySearchEngine activitySearchEngine;
    private final ActivityNotifier activityNotifier;
    private final TagRepository tagRepository;
    private final IdentityProvider identityProvider;

    public ActivityApplicationService(ActivityRepository activityRepository,
                                      ActivityDataSource activityDataSource,
                                      ActivitySearchEngine activitySearchEngine,
                                      ActivityNotifier activityNotifier,
                                      TagRepository tagRepository,
                                      IdentityProvider identityProvider) {

        this.activityRepository = activityRepository;
        this.activityDataSource = activityDataSource;
        this.activitySearchEngine = activitySearchEngine;
        this.activityNotifier = activityNotifier;
        this.tagRepository = tagRepository;
        this.identityProvider = identityProvider;
    }

    public ActivityResult createActivity(CreateActivityCommand createActivityCommand) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, creator);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto newActivityData = new ActivityDto(
                createActivityCommand.activityTitle(),
                createActivityCommand.activityStartTime(),
                createActivityCommand.activityEndTime(),
                createActivityCommand.activityComment(),
                new HashSet<>(createActivityCommand.assignedTags()),
                createActivityCommand.metricValueAssignments().stream()
                        .map(metricValueAssignment -> new MetricValue(
                                metricValueAssignment.metricId(),
                                metricValueAssignment.metricValue()
                        ))
                        .toList()
        );

        Activity activity = Activity.create(newActivityData, creator, tagsExistenceVerifier, metricsExistenceVerifier);
        activityRepository.add(activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), creator)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find created activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public SearchResult<ActivityResult> searchActivities(SearchActivitiesQuery searchActivitiesQuery) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, searcher);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                searchActivitiesQuery.pageSize(),
                searchActivitiesQuery.pageId(),
                searchActivitiesQuery.term(),
                searchActivitiesQuery.timeRangeStart(),
                searchActivitiesQuery.timeRangeEnd(),
                searchActivitiesQuery.excludeFilter(),
                searchActivitiesQuery.tags(),
                null
        );

        EntitySearchResult<ActivityDto> searchResult = activitySearchEngine.findActivities(searchCriteria);
        List<ActivityResult> resultForClient = searchResult.results().stream()
                .map(activity -> Activity.fromStorage(activity, tagsExistenceVerifier, metricsExistenceVerifier))
                .map(activity -> activity.forClient(searchCriteria.searcher()))
                .map(this::toActivityResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public ActivityResult switchToNewActivity(SwitchActivityCommand switchActivityCommand) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User switcher = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, switcher);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityToSwitch = new ActivityDto(
                switchActivityCommand.activityTitle(),
                switchActivityCommand.activityStartTime(),
                switchActivityCommand.activityEndTime(),
                switchActivityCommand.activityComment(),
                new HashSet<>(switchActivityCommand.assignedTags()),
                switchActivityCommand.metricValueAssignments().stream()
                        .map(metricValueAssignment -> new MetricValue(
                                metricValueAssignment.metricId(),
                                metricValueAssignment.metricValue()
                        ))
                        .toList()
        );

        Activity newActivity = Activity.create(activityToSwitch, switcher, tagsExistenceVerifier, metricsExistenceVerifier);
        Instant switchTime = newActivity.isStarted() ? newActivity.startTime() : now();
        newActivity.start(switchTime, switcher);

        List<Activity> activitiesToFinish = activityRepository.findUnfinishedStartedBefore(switchTime, switcher).stream()
                .map(activity -> Activity.fromStorage(activity, tagsExistenceVerifier, metricsExistenceVerifier))
                .toList();

        activitiesToFinish.forEach(activity -> activity.finish(switchTime, switcher));
        activitiesToFinish.stream()
                .map(Activity::forStorage)
                .forEach(activity -> activityRepository.update(activity.id(), activity));

        activityRepository.add(newActivity.forStorage());

        activitiesToFinish.stream()
                .map(Activity::forChangeNotification)
                .forEach(activityNotifier::notifyChanged);
        activityNotifier.notifyChanged(newActivity.forChangeNotification());

        return activityDataSource.find(newActivity.id(), switcher)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find created activity with ID=%s".formatted(newActivity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult renameActivity(String newTitle, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.rename(newTitle, updater);

        activityRepository.update(activityId, activity.forStorage());
        ActivityDto activityResult = activity.forClient(updater);

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult startActivity(Instant startTime, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.start(startTime, updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult finishActivity(Instant endTime, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.finish(endTime, updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult updateActivityComment(String newComment, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.updateComment(newComment, updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult addTagToActivity(UUID tagId, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.assignTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult removeTagFromActivity(UUID tagId, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.removeTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult setMetricValue(UUID metricId, BigDecimal value, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.setMetricValue(new MetricValue(metricId, value), updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult unsetMetricValue(UUID metricId, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.unsetMetricValue(new MetricId(metricId), updater);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public void deleteActivity(UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, remover);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.delete(remover);

        activityNotifier.notifyChanged(activity.forChangeNotification());

        activityRepository.update(activityId, activity.forStorage());
    }

    private ActivityResult toActivityResult(ActivityDto activityDto) {
        List<MetricValueResult> metricValueResults = activityDto.metricValues().stream()
                .map(this::toMetricValueResult)
                .toList();
        return new ActivityResult(
                activityDto.id(),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags(),
                metricValueResults
        );
    }

    private MetricValueResult toMetricValueResult(MetricValue metricValue) {
        return new MetricValueResult(metricValue.metricId(), metricValue.value());
    }
}
