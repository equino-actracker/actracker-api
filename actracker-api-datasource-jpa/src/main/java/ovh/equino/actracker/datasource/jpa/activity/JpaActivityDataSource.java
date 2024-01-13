package ovh.equino.actracker.datasource.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivityDataSource;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityId;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.JpaDAO;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;

class JpaActivityDataSource extends JpaDAO implements ActivityDataSource {

    JpaActivityDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<ActivityDto> find(ActivityId activityId, User searcher) {

        SelectActivityQuery selectActivity = new SelectActivityQuery(entityManager);
        Optional<ActivityProjection> activityResult = selectActivity
                .where(
                        selectActivity.predicate().and(
                                selectActivity.predicate().hasId(activityId.id()),
                                selectActivity.predicate().isAccessibleFor(searcher),
                                selectActivity.predicate().isNotDeleted()
                        )
                )
                .execute();

        SelectActivityJoinTagQuery selectActivityJoinTag = new SelectActivityJoinTagQuery(entityManager);
        Set<UUID> tagIds = selectActivityJoinTag
                .where(
                        selectActivityJoinTag.predicate().and(
                                selectActivityJoinTag.predicate().hasActivityId(activityId.id()),
                                selectActivityJoinTag.predicate().isNotDeleted(),
                                selectActivityJoinTag.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute()
                .stream()
                .map(ActivityJoinTagProjection::tagId)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        SelectMetricValuesQuery selectMetricValues = new SelectMetricValuesQuery(entityManager);
        List<MetricValue> metricValues = selectMetricValues
                .where(
                        selectMetricValues.predicate().and(
                                selectMetricValues.predicate().hasActivityId(activityId.id()),
                                selectMetricValues.predicate().isNotDeleted(),
                                selectMetricValues.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute()
                .stream()
                .map(MetricValueProjection::toMetricValue)
                .toList();

        return activityResult.map(result -> result.toActivity(tagIds, metricValues));
    }

    @Override
    public List<ActivityDto> find(EntitySearchCriteria searchCriteria) {

        Timestamp timeRangeStart = isNull(searchCriteria.timeRangeStart())
                ? null
                : Timestamp.from(searchCriteria.timeRangeStart());
        Timestamp timeRangeEnd = isNull(searchCriteria.timeRangeEnd())
                ? null
                : Timestamp.from(searchCriteria.timeRangeEnd());

        SelectActivitiesQuery selectActivities = new SelectActivitiesQuery(entityManager);
        List<ActivityProjection> activityResults = selectActivities
                .where(
                        selectActivities.predicate().and(
                                selectActivities.predicate().isNotDeleted(),
                                selectActivities.predicate().isAccessibleFor(searchCriteria.searcher()),
                                selectActivities.predicate().isInPage(searchCriteria.pageId()),
                                selectActivities.predicate().isNotExcluded(searchCriteria.excludeFilter()),
                                selectActivities.predicate().hasAnyOfTag(searchCriteria.tags()),
                                selectActivities.predicate().isInTimeRange(timeRangeStart, timeRangeEnd)
                        )
                )
                .orderBy(selectActivities.sort().ascending("id"))
                .limit(searchCriteria.pageSize())
                .execute();

        Set<UUID> foundActivityIds = activityResults
                .stream()
                .map(ActivityProjection::id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        SelectActivityJoinTagQuery selectActivityJoinTag = new SelectActivityJoinTagQuery(entityManager);
        Map<String, Set<UUID>> tagsByActivityId = selectActivityJoinTag
                .where(
                        selectActivityJoinTag.predicate().and(
                                selectActivityJoinTag.predicate().hasActivityIdIn(foundActivityIds),
                                selectActivityJoinTag.predicate().isNotDeleted(),
                                selectActivityJoinTag.predicate().isAccessibleFor(searchCriteria.searcher())
                        )
                )
                .execute()
                .stream()
                .collect(groupingBy(
                        ActivityJoinTagProjection::activityId,
                        mapping(projection -> UUID.fromString(projection.tagId()), toUnmodifiableSet())
                ));

        SelectMetricValuesQuery selectMetricValue = new SelectMetricValuesQuery(entityManager);
        Map<String, List<MetricValue>> metricValues = selectMetricValue
                .where(
                        selectMetricValue.predicate().and(
                                selectMetricValue.predicate().hasActivityIdIn(foundActivityIds),
                                selectMetricValue.predicate().isNotDeleted(),
                                selectMetricValue.predicate().isAccessibleFor(searchCriteria.searcher())
                        )
                )
                .execute()
                .stream()
                .collect(groupingBy(
                        MetricValueProjection::activityId,
                        mapping(MetricValueProjection::toMetricValue, toList())
                ));

        return activityResults
                .stream()
                .map(result -> result.toActivity(
                        tagsByActivityId.getOrDefault(result.id(), emptySet()),
                        metricValues.getOrDefault(result.id(), emptyList())
                ))
                .toList();
    }

    @Override
    public List<ActivityId> findOwnUnfinishedStartedBefore(Instant startTime, User owner) {

        SelectActivitiesQuery selectActivities = new SelectActivitiesQuery(entityManager);
        return selectActivities
                .where(
                        selectActivities.predicate().and(
                                selectActivities.predicate().isOwner(owner),
                                selectActivities.predicate().isNotDeleted(),
                                selectActivities.predicate().isStarted(),
                                selectActivities.predicate().isStartedBeforeOrAt(Timestamp.from(startTime)),
                                selectActivities.predicate().isNotFinished()
                        )
                )
                .execute()
                .stream()
                .map(result -> new ActivityId(UUID.fromString(result.id())))
                .toList();
    }
}
