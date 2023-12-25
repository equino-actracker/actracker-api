package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public class ActivityApplicationService {

    private final ActivityFactory activityFactory;
    private final ActivityRepository activityRepository;
    private final ActivityDataSource activityDataSource;
    private final ActivitySearchEngine activitySearchEngine;
    private final ActivityNotifier activityNotifier;
    private final ActorExtractor actorExtractor;

    public ActivityApplicationService(ActivityFactory activityFactory,
                                      ActivityRepository activityRepository,
                                      ActivityDataSource activityDataSource,
                                      ActivitySearchEngine activitySearchEngine,
                                      ActivityNotifier activityNotifier,
                                      ActorExtractor actorExtractor) {

        this.activityFactory = activityFactory;
        this.activityRepository = activityRepository;
        this.activityDataSource = activityDataSource;
        this.activitySearchEngine = activitySearchEngine;
        this.activityNotifier = activityNotifier;
        this.actorExtractor = actorExtractor;
    }

    public ActivityResult createActivity(CreateActivityCommand createActivityCommand) {
        User creator = actorExtractor.getActor();

        List<TagId> tags = createActivityCommand.assignedTags()
                .stream()
                .map(TagId::new)
                .toList();
        List<MetricValue> metricValues = createActivityCommand.metricValueAssignments()
                .stream()
                .map(metricValueAssignment -> new MetricValue(
                        metricValueAssignment.metricId(),
                        metricValueAssignment.metricValue()
                ))
                .toList();

        Activity activity = activityFactory.create(
                createActivityCommand.activityTitle(),
                createActivityCommand.activityStartTime(),
                createActivityCommand.activityEndTime(),
                createActivityCommand.activityComment(),
                tags,
                metricValues
        );
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

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                actorExtractor.getActor(),
                searchActivitiesQuery.pageSize(),
                searchActivitiesQuery.pageId(),
                searchActivitiesQuery.term(),
                searchActivitiesQuery.timeRangeStart(),
                searchActivitiesQuery.timeRangeEnd(),
                searchActivitiesQuery.excludeFilter(),
                searchActivitiesQuery.tags()
        );

        EntitySearchResult<ActivityDto> searchResult = activitySearchEngine.findActivities(searchCriteria);
        List<ActivityResult> resultForClient = searchResult.results()
                .stream()
                .map(this::toActivityResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public ActivityResult switchToNewActivity(SwitchActivityCommand switchActivityCommand) {
        User switcher = actorExtractor.getActor();

        List<TagId> tags = switchActivityCommand.assignedTags()
                .stream()
                .map(TagId::new)
                .toList();
        List<MetricValue> metricValues = switchActivityCommand.metricValueAssignments()
                .stream()
                .map(metricValueAssignment -> new MetricValue(
                        metricValueAssignment.metricId(),
                        metricValueAssignment.metricValue()
                ))
                .toList();

        Activity newActivity = activityFactory.create(
                switchActivityCommand.activityTitle(),
                switchActivityCommand.activityStartTime(),
                switchActivityCommand.activityEndTime(),
                switchActivityCommand.activityComment(),
                tags,
                metricValues
        );
        Instant switchTime = newActivity.isStarted() ? newActivity.startTime() : now();
        newActivity.start(switchTime);

        List<ActivityId> activitiesToFinish = activityDataSource.findOwnUnfinishedStartedBefore(switchTime, switcher);
        finishAllAt(switchTime, activitiesToFinish);

        activityRepository.add(newActivity.forStorage());
        activityNotifier.notifyChanged(newActivity.forChangeNotification());

        return activityDataSource.find(newActivity.id(), switcher)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find created activity with ID=%s".formatted(newActivity.id());
                    return new RuntimeException(message);
                });
    }

    private void finishAllAt(Instant switchTime, List<ActivityId> activitiesToFinish) {

        for (ActivityId activityId : activitiesToFinish) {
            Activity activityToFinish = activityRepository.findById(activityId.id())
                    .map(activity -> activityFactory.reconstitute(activityId, new User(activity.creatorId()), activity.title(), activity.startTime(), activity.endTime(), activity.comment(), activity.tags().stream().map(TagId::new).toList(), activity.metricValues(), activity.deleted()))
                    .orElseThrow(() -> {
                        String message = "Could not find activity to stop with ID=%s".formatted(activityId.id());
                        return new RuntimeException(message);
                    });
            activityToFinish.finish(switchTime);
            activityRepository.update(activityId.id(), activityToFinish.forStorage());
            activityNotifier.notifyChanged(activityToFinish.forChangeNotification());
        }
    }

    public ActivityResult renameActivity(String newTitle, UUID activityId) {
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.rename(newTitle);

        activityRepository.update(activityId, activity.forStorage());

        activityNotifier.notifyChanged(activity.forChangeNotification());

        return activityDataSource.find(activity.id(), updater)
                .map(this::toActivityResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated activity with ID=%s".formatted(activity.id());
                    return new RuntimeException(message);
                });
    }

    public ActivityResult startActivity(Instant startTime, UUID activityId) {
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.start(startTime);

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
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.finish(endTime);

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
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.updateComment(newComment);

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
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.assignTag(new TagId(tagId));

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
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.removeTag(new TagId(tagId));

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
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.setMetricValue(new MetricValue(metricId, value));

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
        User updater = actorExtractor.getActor();

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.unsetMetricValue(new MetricId(metricId));

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

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = activityFactory.reconstitute(
                new ActivityId(activityDto.id()),
                new User(activityDto.creatorId()),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags().stream().map(TagId::new).toList(),
                activityDto.metricValues(),
                activityDto.deleted()
        );
        activity.delete();

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
