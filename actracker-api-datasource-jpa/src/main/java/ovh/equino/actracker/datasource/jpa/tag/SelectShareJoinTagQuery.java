package ovh.equino.actracker.datasource.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagEntity_;
import ovh.equino.actracker.jpa.tag.TagShareEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity_;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.stream.Collectors.toUnmodifiableSet;

final class SelectShareJoinTagQuery extends MultiResultJpaQuery<TagShareEntity, ShareJoinTagProjection> {

    private final Join<TagShareEntity, TagEntity> tag;
    private final PredicateBuilder predicate;

    SelectShareJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tag = root.join(TagShareEntity_.tag, INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        ShareJoinTagProjection.class,
                        root.get(TagShareEntity_.granteeId),
                        tag.get(TagEntity_.id),
                        root.get(TagShareEntity_.granteeName)
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectShareJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    /**
     * Deprecated: Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<TagShareEntity> sort() {
        throw new RuntimeException("Sorting shares joint with tags not supported");
    }

    @Override
    protected Class<TagShareEntity> getRootEntityType() {
        return TagShareEntity.class;
    }

    @Override
    protected Class<ShareJoinTagProjection> getProjectionType() {
        return ShareJoinTagProjection.class;
    }

    final class PredicateBuilder extends JpaPredicateBuilder<TagShareEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(tag.get(TagEntity_.deleted));
        }

        public JpaPredicate hasTagId(UUID tagId) {
            return () -> criteriaBuilder.equal(tag.get(TagEntity_.id), tagId.toString());
        }

        public JpaPredicate hasTagIdIn(Collection<UUID> tagIds) {
            Set<String> tagIdsAsStrings = tagIds
                    .stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet());
            return in(tagIdsAsStrings, tag.get(TagEntity_.id));
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return () -> criteriaBuilder.equal(tag.get(TagEntity_.creatorId), searcher.id().toString());
        }
    }
}
