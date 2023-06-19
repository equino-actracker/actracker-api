package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivitySortField;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;
import ovh.equino.actracker.repository.jpa.tag.TagShareEntity;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

class ActivityQueryBuilder extends JpaQueryBuilder<ActivityEntity> {

    private static final Map<EntitySortCriteria.Field, String> SORT_FIELD_TO_ENTITY_FIELD =
            Map.of(
                    EntitySortCriteria.CommonSortField.ID, "id",
                    ActivitySortField.START_TIME, "startTime",
                    ActivitySortField.END_TIME, "endTime"
            );

    ActivityQueryBuilder(EntityManager entityManager) {
        super(entityManager, ActivityEntity.class, SORT_FIELD_TO_ENTITY_FIELD);
    }

    Predicate isInTimeRange(Timestamp timeRangeStart, Timestamp timeRangeEnd) {
        Predicate isBeforeRangeEnd = timeRangeEnd != null
                ? isStartedBeforeOrAt(timeRangeEnd)
                : allMatch();

        Predicate isAfterRangeStart = timeRangeStart != null
                //@formatter:off
                ? and(
                    isStarted(),
                    or(
                        isNotFinished(),
                        isFinishedAfterOrAt(timeRangeStart)
                    )
                )
                : allMatch();
                //@formatter:on

        return and(isBeforeRangeEnd, isAfterRangeStart);
    }

    Predicate isStartedBeforeOrAt(Timestamp startTime) {
        return and(
                isStarted(),
                criteriaBuilder.lessThanOrEqualTo(
                        rootEntity.get("startTime"),
                        startTime
                )
        );
    }

    Predicate isFinishedAfterOrAt(Timestamp endTime) {
        return and(
                isFinished(),
                criteriaBuilder.greaterThanOrEqualTo(
                        rootEntity.get("endTime"),
                        endTime
                )
        );
    }

    Predicate isStarted() {
        return criteriaBuilder.isNotNull(
                rootEntity.get("startTime")
        );
    }

    Predicate isFinished() {
        return criteriaBuilder.isNotNull(
                rootEntity.get("endTime")
        );
    }

    Predicate isNotFinished() {
        return criteriaBuilder.isNull(
                rootEntity.get("endTime")
        );
    }

    Predicate hasAnyOfTag(Set<UUID> tags) {
        if (isEmpty(tags)) {
            return allMatch();
        }

        Join<ActivityEntity, TagEntity> activityTag = rootEntity.join("tags");

        Predicate[] predicatesForTags = tags.stream()
                .map(tagId -> hasTag(tagId, activityTag))
                .toArray(Predicate[]::new);

        return or(predicatesForTags);
    }

    private Predicate hasTag(UUID tagId, Join<ActivityEntity, TagEntity> activityTag) {
        Subquery<Long> subQuery = criteriaQuery.subquery(Long.class);
        subQuery.select(criteriaBuilder.literal(1L))
                .where(criteriaBuilder.equal(activityTag.get("id"), tagId.toString()))
                .from(ActivityEntity.class);
        return criteriaBuilder.exists(subQuery);
    }

    @Override
    public Predicate isAccessibleFor(User searcher) {
        return or(
                super.isAccessibleFor(searcher),
                isGrantee(searcher)
        );
    }

    private Predicate isGrantee(User user) {
        Join<ActivityEntity, TagEntity> activityWithTag = rootEntity.join("tags", JoinType.LEFT);
        Join<?, TagShareEntity> sharedActivity = activityWithTag.join("shares", JoinType.LEFT);
        Subquery<Long> subQuery = criteriaQuery.subquery(Long.class);
        subQuery.select(criteriaBuilder.literal(1L))
                .where(criteriaBuilder.equal(sharedActivity.get("granteeId"), user.id().toString()))
                .from(TagEntity.class);
        return criteriaBuilder.exists(subQuery);
    }
}
