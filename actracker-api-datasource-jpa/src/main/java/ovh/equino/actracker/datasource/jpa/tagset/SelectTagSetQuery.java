package ovh.equino.actracker.datasource.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortCriteria;
import ovh.equino.actracker.datasource.jpa.SingleResultJpaQuery;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;
import ovh.equino.actracker.jpa.tagset.TagSetEntity_;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

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
                        root.get(TagSetEntity_.id),
                        root.get(TagSetEntity_.creatorId),
                        root.get(TagSetEntity_.name),
                        root.get(TagSetEntity_.deleted)
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
            return () -> criteriaBuilder.isFalse(root.get(TagSetEntity_.deleted));
        }


        public JpaPredicate isAccessibleFor(User searcher) {
            return isOwner(searcher);
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get(TagSetEntity_.creatorId),
                    searcher.id().toString()
            );
        }

        @Override
        protected Optional<PageableAttribute<? extends Comparable<?>>> entityPageableAttribute(
                EntitySearchPageId.Value pageValue) {

            return Optional.empty();
        }

        @Override
        protected List<JpaSortCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            return emptyList();
        }

        @Override
        protected Optional<Expression<?>> entitySortableAttribute(EntitySortCriteria.Level sortCriterion) {
            return Optional.empty();
        }

        @Override
        protected List<PageableAttribute<? extends Comparable<?>>> toEntityPageConditions(EntitySearchPageId.Value pageAttribute) {
            return emptyList();
        }
    }
}
