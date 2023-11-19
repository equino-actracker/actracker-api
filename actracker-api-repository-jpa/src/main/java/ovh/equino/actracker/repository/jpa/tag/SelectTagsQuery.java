package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

final class SelectTagsQuery extends MultiResultJpaQuery<TagEntity, TagProjection> {

    private final PredicateBuilder predicate;
    private final SortBuilder sort;

    SelectTagsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
        this.sort = new SortBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                TagProjection.class,
                                root.get("id"),
                                root.get("creatorId"),
                                root.get("name"),
                                root.get("deleted")
                        )
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public JpaSortBuilder<TagEntity> sort() {
        return sort;
    }

    @Override
    public SelectTagsQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TagEntity> getRootEntityType() {
        return TagEntity.class;
    }

    @Override
    protected Class<TagProjection> getProjectionType() {
        return TagProjection.class;
    }

    public final class PredicateBuilder extends JpaPredicateBuilder<TagEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        JpaPredicate matchesTerm(String term) {
            return super.matchesTerm(term, "name");
        }

        @Override
        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    super.isAccessibleFor(searcher),
                    isGrantee(searcher)
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<TagEntity, TagShareEntity> sharedTag = root.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(sharedTag.get("granteeId"), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }

    public final class SortBuilder extends JpaSortBuilder<TagEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
