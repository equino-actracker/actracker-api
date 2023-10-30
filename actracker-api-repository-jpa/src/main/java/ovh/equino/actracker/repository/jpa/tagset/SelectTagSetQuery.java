package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.SingleResultJpaQuery;

final class SelectTagSetQuery extends SingleResultJpaQuery<TagSetEntity, TagSetProjection> {

    private final PredicateBuilder predicateBuilder;

    SelectTagSetQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
    }

    @Override
    protected void initQuery() {
        query.select(
                this.criteriaBuilder.construct(
                        TagSetProjection.class,
                        root.get("id"),
                        root.get("creatorId"),
                        root.get("name"),
                        root.get("deleted")
                )
        );
    }

    @Override
    public PredicateBuilder predicateBuilder() {
        return predicateBuilder;
    }

    @Override
    public SelectTagSetQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TagSetEntity> getRootEntityType() {
        return TagSetEntity.class;
    }

    @Override
    protected Class<TagSetProjection> getProjectionType() {
        return TagSetProjection.class;
    }

    public class PredicateBuilder extends JpaPredicateBuilder<TagSetEntity> {

        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
