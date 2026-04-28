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
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;

final class SelectTagsQuery extends MultiResultJpaQuery<TagEntity, TagProjection> {

    private final PredicateBuilder predicate;
    private final SortBuilder sort;

    private final Expression<String> tagNameLowerCase;
    private final Expression<Integer> tagNameNullWeight;


    SelectTagsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
        this.sort = new SortBuilder();

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
        protected List<JpaSortCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                return switch (sortableAttribute) {
                    case NAME -> nameOrderCriteria(sortCriterion);
                };
            }
            return emptyList();
        }

        private List<JpaSortCriteria> nameOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            var nullFirstOrder = (JpaSortCriteria) () -> criteriaBuilder.asc(tagNameNullWeight);
            var nonNullOrder = DESC == sortCriterion.order()
                    ? (JpaSortCriteria) () -> criteriaBuilder.desc(tagNameLowerCase)
                    : (JpaSortCriteria) () -> criteriaBuilder.asc(tagNameLowerCase);
            return List.of(nullFirstOrder, nonNullOrder);
        }

        @Override
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                return switch (sortableAttribute) {
                    case NAME -> nameCondition(pageAttribute);

                };
            }
            return emptyList();
        }

        private List<PageCondition<? extends Comparable<?>>> nameCondition(EntitySearchPageId.Value pageAttribute) {
            if (isNull(pageAttribute.value())) {
                return singletonList(PageCondition.of(tagNameNullWeight, 0, PageCondition.Relation.GTE));
            }

            var pageValue = pageAttribute.value().toString().toLowerCase();
            var relation = PageCondition.Relation.from(pageAttribute.sortOrder());
            return List.of(
                    PageCondition.of(tagNameNullWeight, 1, PageCondition.Relation.GTE),
                    PageCondition.of(tagNameLowerCase, pageValue, relation)
            );
        }
    }

    public final class SortBuilder extends JpaSortBuilder<TagEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
