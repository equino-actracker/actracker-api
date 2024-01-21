package ovh.equino.actracker.datasource.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.SingleResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;

final class SelectTagSetQuery extends SingleResultJpaQuery<TagSetEntity, TagSetProjection> {

    private final PredicateBuilder predicateBuilder;

    SelectTagSetQuery(EntityManager entityManager) {
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

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get("deleted"));
        }


        public JpaPredicate isAccessibleFor(User searcher) {
            return isOwner(searcher);
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get("creatorId"),
                    searcher.id().toString()
            );
        }
    }
}
