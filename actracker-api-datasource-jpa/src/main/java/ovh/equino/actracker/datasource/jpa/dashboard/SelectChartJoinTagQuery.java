package ovh.equino.actracker.datasource.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.ChartEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;

import java.util.Collection;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;

final class SelectChartJoinTagQuery extends MultiResultJpaQuery<ChartEntity, ChartJoinTagProjection> {

    private final PredicateBuilder predicate;
    private final Join<ChartEntity, ?> tags;

    SelectChartJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tags = root.join("tags", INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        ChartJoinTagProjection.class,
                        root.get("id"),
                        tags.get("id")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectChartJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    /**
     * @deprecated Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<ChartEntity> sort() {
        throw new RuntimeException("Sorting charts joint with tags not supported");
    }

    @Override
    protected Class<ChartEntity> getRootEntityType() {
        return ChartEntity.class;
    }

    @Override
    protected Class<ChartJoinTagProjection> getProjectionType() {
        return ChartJoinTagProjection.class;
    }

    public final class PredicateBuilder extends JpaPredicateBuilder<ChartEntity> {
        PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return and(
                    () -> criteriaBuilder.isFalse(root.get("deleted")),
                    () -> criteriaBuilder.isFalse(tags.get("deleted"))
            );
        }

        @Override
        public JpaPredicate isAccessibleFor(User searcher) {
            return isTagAccessibleFor(searcher);
        }

        JpaPredicate hasChartIdIn(Collection<UUID> chartIds) {
            return super.hasIdIn(chartIds);
        }

        private JpaPredicate isTagAccessibleFor(User user) {
            return or(
                    () -> criteriaBuilder.equal(tags.get("creatorId"), user.id().toString()),
                    isTagSharedWith(user)
            );
        }

        private JpaPredicate isTagSharedWith(User user) {
            Join<?, ?> shares = tags.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(shares.get("granteeId"), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }
}
