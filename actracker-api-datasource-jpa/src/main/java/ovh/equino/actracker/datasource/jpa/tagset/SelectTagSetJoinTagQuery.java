package ovh.equino.actracker.datasource.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;

import java.util.Collection;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;

final class SelectTagSetJoinTagQuery extends MultiResultJpaQuery<TagSetEntity, TagSetJoinTagProjection> {

    private final Join<TagSetEntity, ?> tag;
    private final PredicateBuilder predicateBuilder;

    SelectTagSetJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tag = root.join("tags", INNER);
        this.predicateBuilder = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                this.criteriaBuilder.construct(
                        TagSetJoinTagProjection.class,
                        tag.get("id"),
                        root.get("id")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    /**
     * Deprecated: Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<TagSetEntity> sort() {
        throw new RuntimeException("Sorting tag sets joint with tags not supported");
    }

    @Override
    public SelectTagSetJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TagSetEntity> getRootEntityType() {
        return TagSetEntity.class;
    }

    @Override
    protected Class<TagSetJoinTagProjection> getProjectionType() {
        return TagSetJoinTagProjection.class;
    }

    public class PredicateBuilder extends JpaPredicateBuilder<TagSetEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return and(
                    () -> criteriaBuilder.isFalse(root.get("deleted")),
                    () -> criteriaBuilder.isFalse(tag.get("deleted"))
            );
        }

        public JpaPredicate isAccessibleFor(User user) {
            return and(
                    isOwner(user),
                    isTagAccessibleFor(user)
            );
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get("creatorId"),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isTagAccessibleFor(User user) {
            return or(
                    () -> criteriaBuilder.equal(tag.get("creatorId"), user.id().toString()),
                    isTagSharedWith(user)
            );
        }

        private JpaPredicate isTagSharedWith(User user) {
            Join<?, ?> shares = tag.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(shares.get("granteeId"), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }

        public JpaPredicate hasTagSetId(UUID tagSetId) {
            return super.hasId(tagSetId);
        }

        public JpaPredicate hasTagSetIdIn(Collection<UUID> tagSetIds) {
            return super.hasIdIn(tagSetIds);
        }
    }
}
