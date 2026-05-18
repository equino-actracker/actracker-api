package ovh.equino.actracker.datasource.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.*;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.activity.ActivityEntity;
import ovh.equino.actracker.jpa.activity.ActivityEntity_;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagEntity_;
import ovh.equino.actracker.jpa.tag.TagShareEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity_;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

final class SelectActivitiesQuery extends MultiResultJpaQuery<ActivityEntity, ActivityProjection> {

    private final PredicateBuilder predicateBuilder;
    private final OrderBuilder orderBuilder;

    private final Expression<String> activityTitleLowerCase;
    private final Expression<Integer> activityTitleNullWeight;
    private final Expression<Integer> activityEndTimeNullWeight;


    SelectActivitiesQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
        this.orderBuilder = new OrderBuilder();

        this.activityTitleLowerCase = criteriaBuilder.lower(root.get(ActivityEntity_.title));
        this.activityTitleNullWeight = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(root.get(ActivityEntity_.title)), 0)
                .otherwise(1)
                .as(Integer.class);
        this.activityEndTimeNullWeight = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(root.get(ActivityEntity_.endTime)), 0)
                .otherwise(1)
                .as(Integer.class);
    }

    @Override
    protected void initProjection() {
        query
                .select(
                        this.criteriaBuilder.construct(
                                ActivityProjection.class,
                                root.get(ActivityEntity_.id),
                                root.get(ActivityEntity_.creatorId),
                                root.get(ActivityEntity_.title),
                                activityTitleLowerCase,
                                activityTitleNullWeight,
                                root.get(ActivityEntity_.startTime),
                                root.get(ActivityEntity_.endTime),
                                activityEndTimeNullWeight,
                                root.get(ActivityEntity_.comment),
                                root.get(ActivityEntity_.deleted)
                        )
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    @Override
    public OrderBuilder order() {
        return orderBuilder;
    }

    @Override
    public SelectActivitiesQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<ActivityEntity> getRootEntityType() {
        return ActivityEntity.class;
    }

    @Override
    protected Class<ActivityProjection> getProjectionType() {
        return ActivityProjection.class;
    }

    final class PredicateBuilder extends JpaPredicateBuilder<ActivityEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get(ActivityEntity_.deleted));
        }

        public JpaPredicate isInTimeRange(Timestamp timeRangeStart, Timestamp timeRangeEnd) {
            JpaPredicate endTimeInRange = timeRangeStart != null
                    ? or(not(isFinished()), not(isFinishedBefore(timeRangeStart)))
                    : allMatch();
            JpaPredicate startTimeInRange = timeRangeEnd != null
                    ? or(not(isStarted()), not(isStartedAfter(timeRangeEnd)))
                    : allMatch();
            return and(startTimeInRange, endTimeInRange);
        }

        JpaPredicate isStartedBeforeOrAt(Timestamp startTime) {
            return () -> criteriaBuilder.lessThanOrEqualTo(root.get(ActivityEntity_.startTime), startTime);
        }

        private JpaPredicate isStartedAfter(Timestamp startTime) {
            return () -> criteriaBuilder.greaterThan(root.get(ActivityEntity_.startTime), startTime);
        }

        private JpaPredicate isFinishedBefore(Timestamp endTime) {
            return () -> criteriaBuilder.lessThan(root.get(ActivityEntity_.endTime), endTime);
        }

        private JpaPredicate isFinished() {
            return () -> criteriaBuilder.isNotNull(root.get(ActivityEntity_.endTime));
        }

        JpaPredicate isStarted() {
            return () -> criteriaBuilder.isNotNull(root.get(ActivityEntity_.startTime));
        }

        JpaPredicate isNotFinished() {
            return not(isFinished());
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    isOwner(searcher),
                    isGrantee(searcher)
            );
        }

        public JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get(ActivityEntity_.creatorId),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<ActivityEntity, TagEntity> tags = root.join(ActivityEntity_.tags, JoinType.LEFT);
            Join<TagEntity, TagShareEntity> shares = tags.join(TagEntity_.shares, JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(shares.get(TagShareEntity_.granteeId), user.id().toString()),
                                    criteriaBuilder.isFalse(tags.get(TagEntity_.deleted))
                            )
                    )
                    .from(ActivityEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }

        public JpaPredicate hasAnyOfTag(Set<UUID> requiredTags) {
            if (isEmpty(requiredTags)) {
                return allMatch();
            }

            Join<ActivityEntity, TagEntity> tags = root.join(ActivityEntity_.tags);

            JpaPredicate[] predicatesForTags = requiredTags.stream()
                    .map(tagId -> hasTag(tagId, tags))
                    .toArray(JpaPredicate[]::new);

            return or(predicatesForTags);
        }

        private JpaPredicate hasTag(UUID tagId, Join<ActivityEntity, TagEntity> tags) {
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(tags.get(TagEntity_.id), tagId.toString()),
                                    criteriaBuilder.isFalse(tags.get(TagEntity_.deleted))
                            )
                    )
                    .from(ActivityEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }

        @Override
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof ActivitySearchCriteria.SortableField sortableAttribute) {
                var sortDirection = pageAttribute.sortOrder();
                return switch (sortableAttribute) {
                    case TITLE -> nullFirstPageConditions(
                            activityTitleLowerCase,
                            activityTitleNullWeight,
                            nullableStringLowerCase(pageAttribute),
                            sortDirection
                    );
                    case END_TIME -> nullFirstPageConditions(
                            root.get(ActivityEntity_.endTime).as(Date.class),
                            activityEndTimeNullWeight,
                            nullableTimestamp(pageAttribute),
                            sortDirection
                    );
                };
            }
            return emptyList();
        }
    }

    public class OrderBuilder extends JpaOrderBuilder<ActivityEntity> {
        private OrderBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        protected List<JpaOrderCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof ActivitySearchCriteria.SortableField sortableAttribute) {
                var sortDirection = sortCriterion.order();
                return switch (sortableAttribute) {
                    case TITLE -> nullFirstOrderCriteria(
                            activityTitleLowerCase,
                            activityTitleNullWeight,
                            sortDirection
                    );
                    case END_TIME -> nullFirstOrderCriteria(
                            root.get(ActivityEntity_.endTime),
                            activityEndTimeNullWeight,
                            sortDirection
                    );
                };
            }
            return emptyList();
        }
    }
}
