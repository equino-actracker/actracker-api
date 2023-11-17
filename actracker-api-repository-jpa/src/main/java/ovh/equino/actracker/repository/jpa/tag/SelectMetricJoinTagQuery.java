package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

final class SelectMetricJoinTagQuery extends MultiResultJpaQuery<MetricEntity, MetricJoinTagProjection> {

    private final PredicateBuilder predicate;
    private final Join<MetricEntity, TagEntity> tag;

    SelectMetricJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
        this.tag = root.join("tag", INNER);
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        MetricJoinTagProjection.class,
                        root.get("id"),
                        root.get("creatorId"),
                        root.get("name"),
                        root.get("type"),
                        tag.get("id"),
                        root.get("deleted")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return this.predicate;
    }

    @Override
    public SelectMetricJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<MetricEntity> getRootEntityType() {
        return MetricEntity.class;
    }

    @Override
    protected Class<MetricJoinTagProjection> getProjectionType() {
        return MetricJoinTagProjection.class;
    }

    /**
     * Deprecated: Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<MetricEntity> sort() {
        throw new RuntimeException("Sorting metrics joint with tags not supported");
    }

    public final class PredicateBuilder extends JpaPredicateBuilder<MetricEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate hasTagId(UUID tagId) {
            return () -> criteriaBuilder.equal(tag.get("id"), tagId.toString());
        }

        public JpaPredicate hasTagIdIn(Collection<UUID> tagIds) {
            Set<String> tagIdsAsStrings = tagIds
                    .stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet());
            return in(tagIdsAsStrings, tag.get("id"));

        }
    }
}
