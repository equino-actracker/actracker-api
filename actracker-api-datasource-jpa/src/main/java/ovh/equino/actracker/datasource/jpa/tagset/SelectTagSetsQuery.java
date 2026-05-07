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
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;

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
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof TagSetSearchCriteria.SortableField sortableAttribute) {
                return switch (sortableAttribute) {
                    case NAME -> nameCondition(pageAttribute);

                };
            }
            return emptyList();
        }

        // TODO refactor, extract method for null first condition
        private List<PageCondition<? extends Comparable<?>>> nameCondition(EntitySearchPageId.Value pageAttribute) {
            if (isNull(pageAttribute.value())) {
                return singletonList(PageCondition.of(tagSetNameNullWeight, 0, PageCondition.Relation.GTE));
            }

            var pageValue = pageAttribute.value().toString().toLowerCase();
            var relation = PageCondition.Relation.from(pageAttribute.sortOrder());
            return List.of(
                    PageCondition.of(tagSetNameNullWeight, 1, PageCondition.Relation.GTE),
                    PageCondition.of(tagSetNameLowerCase, pageValue, relation)
            );
        }
    }

    public class OrderBuilder extends JpaOrderBuilder<TagSetEntity> {
        private OrderBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        protected List<JpaOrderCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof TagSetSearchCriteria.SortableField sortableAttribute) {
                return switch (sortableAttribute) {
                    case NAME -> nameOrderCriteria(sortCriterion);
                };
            }
            return emptyList();
        }

        // TODO refactor, extract method for null first sort
        private List<JpaOrderCriteria> nameOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            var nullFirstOrder = (JpaOrderCriteria) () -> criteriaBuilder.asc(tagSetNameNullWeight);
            var nonNullOrder = DESC == sortCriterion.order()
                    ? (JpaOrderCriteria) () -> criteriaBuilder.desc(tagSetNameLowerCase)
                    : (JpaOrderCriteria) () -> criteriaBuilder.asc(tagSetNameLowerCase);
            return List.of(nullFirstOrder, nonNullOrder);
        }
    }
}
