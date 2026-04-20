package ovh.equino.actracker.datasource.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Path;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;
import ovh.equino.actracker.jpa.tagset.TagSetEntity_;

import java.util.Optional;

final class SelectTagSetsQuery extends MultiResultJpaQuery<TagSetEntity, TagSetProjection> {

    private final PredicateBuilder predicateBuilder;
    private final SortBuilder sortBuilder;

    SelectTagSetsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
        this.sortBuilder = new SortBuilder();
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
    public JpaSortBuilder<TagSetEntity> sort() {
        return sortBuilder;
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
        protected Optional<PageableValue<? extends Comparable<?>>> entityPageableValue(
                EntitySearchPageId.Value pageValue) {

//            if (pageValue.sortField() instanceof TagSetSearchCriteria.SortableField sortableField) {
//                return switch (sortableField) {
//                    case NAME -> Optional.of(PageableValue.of(
//                            root.get(TagSetEntity_.name),
//                            (String) pageValue.value(),
//                            PageableValue.PagingDirection.from(pageValue.sortOrder())
//                    ));
//                };
//            }
            return Optional.empty();
        }

        @Override
        protected Optional<Path<?>> entitySortableField(EntitySortCriteria.Field field) {
//            if (field instanceof TagSetSearchCriteria.SortableField sortableField) {
//                return switch (sortableField) {
//                    case NAME -> Optional.of(root.get(TagSetEntity_.name));
//                };
//            }
            return Optional.empty();

        }
    }

    public class SortBuilder extends JpaSortBuilder<TagSetEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
