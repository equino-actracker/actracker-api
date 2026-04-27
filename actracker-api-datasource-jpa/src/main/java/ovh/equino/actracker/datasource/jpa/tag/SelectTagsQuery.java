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
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder.GREATEST_STRING;
import static ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder.LOWEST_STRING;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;

final class SelectTagsQuery extends MultiResultJpaQuery<TagEntity, TagProjection> {

    private final PredicateBuilder predicate;
    private final SortBuilder sort;

    // TODO remove
    private final Expression<String> tagNameAscendingSortableAttribute;
    // TODO remove
    private final Expression<String> tagNameDescendingSortableAttribute;

    private final Expression<String> tagNameLowerCase;
    private final Expression<Integer> tagNameNullWeight;


    SelectTagsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
        this.sort = new SortBuilder();

        this.tagNameAscendingSortableAttribute = criteriaBuilder.lower(
                criteriaBuilder.coalesce(
                        root.get(TagEntity_.name),
                        LOWEST_STRING
                )
        );
        this.tagNameDescendingSortableAttribute = criteriaBuilder.lower(
                criteriaBuilder.coalesce(
                        root.get(TagEntity_.name),
                        GREATEST_STRING
                )
        );
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
                                tagNameAscendingSortableAttribute,
                                tagNameDescendingSortableAttribute,
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
                    case NAME -> {
                        var nullFirstOrder = (JpaSortCriteria) () -> criteriaBuilder.asc(tagNameNullWeight);
                        yield DESC == sortCriterion.order()
                                ? List.of(nullFirstOrder, () -> criteriaBuilder.desc(tagNameLowerCase))
                                : List.of(nullFirstOrder, () -> criteriaBuilder.asc(tagNameLowerCase));
                    }
                };
            }
            return emptyList();
        }

        @Override
        protected Optional<Expression<?>> entitySortableAttribute(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                return switch (sortableAttribute) {
                    case NAME -> DESC == sortCriterion.order()
                            ? Optional.of(tagNameDescendingSortableAttribute)
                            : Optional.of(tagNameAscendingSortableAttribute);
                };
            }
            return Optional.empty();
        }

        @Override
        protected Optional<PageableAttribute<? extends Comparable<?>>> entityPageableAttribute(
                EntitySearchPageId.Value pageValue) {

            if (pageValue.sortField() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = pageValue.sortOrder();
                return switch (sortableAttribute) {
                    case NAME -> Optional.of(PageableAttribute.of(
                            sortDirection == DESC
                                    ? tagNameDescendingSortableAttribute
                                    : tagNameAscendingSortableAttribute,
                            sortDirection == DESC
                                    ? requireNonNullElse(pageValue.value(), GREATEST_STRING).toString().toLowerCase()
                                    : requireNonNullElse(pageValue.value(), LOWEST_STRING).toString().toLowerCase(),
                            PageableAttribute.Relation.from(sortDirection)
                    ));
                };
            }
            return Optional.empty();
        }

        @Override
        protected List<PageableAttribute<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof TagSearchCriteria.SortableField sortableAttribute) {
                return switch (sortableAttribute) {
                    case NAME -> nameCondition(pageAttribute);

                };
            }
            return emptyList();
        }

        private List<PageableAttribute<? extends Comparable<?>>> nameCondition(EntitySearchPageId.Value pageAttribute) {
            if (isNull(pageAttribute.value())) {
                return singletonList(PageableAttribute.of(tagNameNullWeight, 0, PageableAttribute.Relation.GTE));
            }

            var pageValue = pageAttribute.value().toString().toLowerCase();
            var relation = PageableAttribute.Relation.from(pageAttribute.sortOrder());
            return List.of(
                    PageableAttribute.of(tagNameNullWeight, 1, PageableAttribute.Relation.GTE),
                    PageableAttribute.of(tagNameLowerCase, pageValue, relation)
            );
        }
    }

    public final class SortBuilder extends JpaSortBuilder<TagEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
