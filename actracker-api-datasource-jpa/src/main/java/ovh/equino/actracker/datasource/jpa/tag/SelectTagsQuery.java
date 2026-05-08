package ovh.equino.actracker.datasource.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.*;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagEntity_;
import ovh.equino.actracker.jpa.tag.TagShareEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity_;

import java.util.List;

import static java.util.Collections.emptyList;

final class SelectTagsQuery extends MultiResultJpaQuery<TagEntity, TagProjection> {

    private final PredicateBuilder predicateBuilder;
    private final OrderBuilder orderBuilder;

    private final Expression<String> tagNameLowerCase;
    private final Expression<Integer> tagNameNullWeight;

    SelectTagsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
        this.orderBuilder = new OrderBuilder();

        this.tagNameLowerCase = criteriaBuilder.lower(root.get(TagEntity_.name));
        this.tagNameNullWeight = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(root.get(TagEntity_.name)), 0)
                .otherwise(1)
                .as(Integer.class);
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                TagProjection.class,
                                root.get(TagEntity_.id),
                                root.get(TagEntity_.creatorId),
                                root.get(TagEntity_.name),
                                tagNameLowerCase,
                                tagNameNullWeight,
                                root.get(TagEntity_.deleted)
                        )
                )
                .distinct(true);    // TODO Find a way to remove it, then remove sortable attributes from TagProjection
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    @Override
    public JpaOrderBuilder<TagEntity> order() {
        return orderBuilder;
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

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get(TagEntity_.deleted));
        }

        JpaPredicate matchesTerm(String term) {
            return super.matchesTerm(term, root.get(TagEntity_.name));
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    isOwner(searcher),
                    isGrantee(searcher)
            );
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get(TagEntity_.creatorId),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<TagEntity, TagShareEntity> sharedTag = root.join(TagEntity_.shares, JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(sharedTag.get(TagShareEntity_.granteeId), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }

        @Override
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = pageAttribute.sortOrder();
                return switch (sortableAttribute) {
                    case NAME -> nullFirstPageCondition(
                            tagNameLowerCase,
                            tagNameNullWeight,
                            nullableValueLowerCase(pageAttribute),
                            sortDirection
                    );
                };
            }
            return emptyList();
        }
    }

    public final class OrderBuilder extends JpaOrderBuilder<TagEntity> {
        private OrderBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        protected List<JpaOrderCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = sortCriterion.order();
                return switch (sortableAttribute) {
                    case NAME -> nullFirstOrderCriteria(tagNameLowerCase, tagNameNullWeight, sortDirection);
                };
            }
            return emptyList();
        }
    }
}
