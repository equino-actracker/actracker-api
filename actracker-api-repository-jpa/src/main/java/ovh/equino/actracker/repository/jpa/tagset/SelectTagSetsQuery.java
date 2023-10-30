package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

class SelectTagSetsQuery extends MultiResultJpaQuery<TagSetEntity, TagSetProjection> {

    private final PredicateBuilder predicateBuilder;

    SelectTagSetsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
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
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    @Override
    public SelectTagSetsQuery where(JpaPredicate predicate) {
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
