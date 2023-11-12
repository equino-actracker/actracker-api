package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivityDataSource;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

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
        List<ActivityJoinTagProjection> activityJoinTag = selectActivityJoinTag
                .where(
                        selectActivityJoinTag.predicate().and(
                                selectActivityJoinTag.predicate().hasActivityId(activityId.id()),
                                selectActivityJoinTag.predicate().isNotDeleted(),
                                selectActivityJoinTag.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute();

        Set<UUID> tagIdsForActivity = activityJoinTag
                .stream()
                .map(ActivityJoinTagProjection::tagId)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        return activityResult
                .map(result -> toActivity(result, tagIdsForActivity));
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

        return activityResults
                .stream()
                .map(activityProjection -> toActivity(activityProjection, emptySet()))
                .toList();
    }

    private ActivityDto toActivity(ActivityProjection activityProjection, Set<UUID> tagIds) {
        return new ActivityDto(
                UUID.fromString(activityProjection.id()),
                UUID.fromString(activityProjection.creatorId()),
                activityProjection.title(),
                isNull(activityProjection.startTime()) ? null : activityProjection.startTime().toInstant(),
                isNull(activityProjection.endTime()) ? null : activityProjection.endTime().toInstant(),
                activityProjection.comment(),
                tagIds,
                null,
                activityProjection.deleted()
        );
    }
}
