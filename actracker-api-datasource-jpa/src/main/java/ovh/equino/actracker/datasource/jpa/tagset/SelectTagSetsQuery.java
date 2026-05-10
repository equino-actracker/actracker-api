package ovh.equino.actracker.datasource.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import ovh.equino.actracker.datasource.jpa.*;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;
import ovh.equino.actracker.jpa.tagset.TagSetEntity_;

import java.util.List;

import static java.util.Collections.emptyList;

final class SelectTagSetsQuery extends MultiResultJpaQuery<TagSetEntity, TagSetProjection> {

    private final PredicateBuilder predicateBuilder;
    private final OrderBuilder orderBuilder;

    private final Expression<String> tagSetNameLowerCase;
    private final Expression<Integer> tagSetNameNullWeight;

    SelectTagSetsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
        this.orderBuilder = new OrderBuilder();

        this.tagSetNameLowerCase = criteriaBuilder.lower(root.get(TagSetEntity_.name));
        this.tagSetNameNullWeight = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(root.get(TagSetEntity_.name)), 0)
                .otherwise(1)
                .as(Integer.class);
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
    public JpaOrderBuilder<TagSetEntity> order() {
        return orderBuilder;
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
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof TagSetSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = pageAttribute.sortOrder();
                return switch (sortableAttribute) {
                    case NAME -> nullFirstPageConditions(
                            tagSetNameLowerCase,
                            tagSetNameNullWeight,
                            nullableValueLowerCase(pageAttribute),
                            sortDirection
                    );
                };
            }
            return emptyList();
        }
    }

    public class OrderBuilder extends JpaOrderBuilder<TagSetEntity> {
        private OrderBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        protected List<JpaOrderCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof TagSetSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = sortCriterion.order();
                return switch (sortableAttribute) {
                    case NAME -> nullFirstOrderCriteria(tagSetNameLowerCase, tagSetNameNullWeight, sortDirection);
                };
            }
            return emptyList();
        }
    }
}
