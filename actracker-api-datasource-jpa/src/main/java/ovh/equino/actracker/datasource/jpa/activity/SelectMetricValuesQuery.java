package ovh.equino.actracker.datasource.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.activity.ActivityEntity;
import ovh.equino.actracker.jpa.activity.MetricValueEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;

import java.util.Collection;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

final class SelectMetricValuesQuery extends MultiResultJpaQuery<MetricValueEntity, MetricValueProjection> {

    private final PredicateBuilder predicate;
    private final Join<MetricValueEntity, ActivityEntity> activity;
    private final Join<MetricValueEntity, ?> metric;
    private final Join<?, ?> tag;

    SelectMetricValuesQuery(EntityManager entityManager) {
        super(entityManager);
        this.activity = root.join("activity", INNER);
        this.metric = root.join("metric", INNER);
        this.tag = metric.join("tag", INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                MetricValueProjection.class,
                                root.get("id"),
                                activity.get("id"),
                                metric.get("id"),
                                root.get("value")
                        )
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectMetricValuesQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<MetricValueEntity> getRootEntityType() {
        return MetricValueEntity.class;
    }

    @Override
    protected Class<MetricValueProjection> getProjectionType() {
        return MetricValueProjection.class;
    }

    /**
     * @deprecated Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<MetricValueEntity> sort() {
        throw new RuntimeException("Sorting metric values not supported");
    }

    public class PredicateBuilder extends JpaPredicateBuilder<MetricValueEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate hasActivityId(UUID activityId) {
            return () -> criteriaBuilder.equal(activity.get("id"), activityId.toString());
        }

        public JpaPredicate hasActivityIdIn(Collection<UUID> activityIds) {
            if (isEmpty(activityIds)) {
                return noneMatch();
            }
            Path<Object> activityId = activity.get("id");
            CriteriaBuilder.In<Object> activityIdIn = criteriaBuilder.in(activityId);
            activityIds.stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet())
                    .forEach(activityIdIn::value);
            return () -> activityIdIn;
        }

        @Override
        public JpaPredicate isAccessibleFor(User user) {
            return isTagAccessibleFor(user);
        }

        private JpaPredicate isTagAccessibleFor(User user) {
            return or(
                    () -> criteriaBuilder.equal(tag.get("creatorId"), user.id().toString()),
                    isTagSharedWith(user)
            );
        }

        private JpaPredicate isTagSharedWith(User user) {
            Join<?, ?> shares = tag.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(shares.get("granteeId"), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }

        public JpaPredicate isNotDeleted() {
            return and(
                    () -> criteriaBuilder.isFalse(metric.get("deleted")),
                    () -> criteriaBuilder.isFalse(tag.get("deleted"))
            );
        }
    }
}
