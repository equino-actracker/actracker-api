package ovh.equino.actracker.datasource.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.activity.ActivityEntity;
import ovh.equino.actracker.jpa.activity.ActivityEntity_;
import ovh.equino.actracker.jpa.activity.MetricValueEntity;
import ovh.equino.actracker.jpa.activity.MetricValueEntity_;
import ovh.equino.actracker.jpa.tag.*;

import java.util.Collection;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

final class SelectMetricValuesQuery extends MultiResultJpaQuery<MetricValueEntity, MetricValueProjection> {

    private final PredicateBuilder predicate;
    private final Join<MetricValueEntity, ActivityEntity> activity;
    private final Join<MetricValueEntity, MetricEntity> metric;
    private final Join<MetricEntity, TagEntity> tag;

    SelectMetricValuesQuery(EntityManager entityManager) {
        super(entityManager);
        this.activity = root.join(MetricValueEntity_.activity, INNER);
        this.metric = root.join(MetricValueEntity_.metric, INNER);
        this.tag = metric.join(MetricEntity_.tag, INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                MetricValueProjection.class,
                                root.get(MetricValueEntity_.id),
                                activity.get(ActivityEntity_.id),
                                metric.get(MetricEntity_.id),
                                root.get(MetricValueEntity_.value)
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
            return () -> criteriaBuilder.equal(activity.get(ActivityEntity_.id), activityId.toString());
        }

        public JpaPredicate hasActivityIdIn(Collection<UUID> activityIds) {
            if (isEmpty(activityIds)) {
                return noneMatch();
            }
            Path<String> activityId = activity.get(ActivityEntity_.id);
            CriteriaBuilder.In<String> activityIdIn = criteriaBuilder.in(activityId);
            activityIds.stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet())
                    .forEach(activityIdIn::value);
            return () -> activityIdIn;
        }

        public JpaPredicate isAccessibleFor(User user) {
            return isTagAccessibleFor(user);
        }

        private JpaPredicate isTagAccessibleFor(User user) {
            return or(
                    () -> criteriaBuilder.equal(tag.get(TagEntity_.creatorId), user.id().toString()),
                    isTagSharedWith(user)
            );
        }

        private JpaPredicate isTagSharedWith(User user) {
            Join<TagEntity, TagShareEntity> shares = tag.join(TagEntity_.shares, JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(shares.get(TagShareEntity_.granteeId), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }

        public JpaPredicate isNotDeleted() {
            return and(
                    () -> criteriaBuilder.isFalse(metric.get(MetricEntity_.deleted)),
                    () -> criteriaBuilder.isFalse(tag.get(TagEntity_.deleted))
            );
        }
    }
}
