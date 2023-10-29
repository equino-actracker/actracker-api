package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.SingleResultJpaQuery;

final class SelectTagSetQuery extends SingleResultJpaQuery<TagSetEntity, TagSetProjection> {

    SelectTagSetQuery(EntityManager entityManager) {
        super(entityManager);
        query.select(
                criteriaBuilder.construct(
                        TagSetProjection.class,
                        root.get("id"),
                        root.get("creatorId"),
                        root.get("name"),
                        root.get("deleted")
                )
        );
    }

    @Override
    public SelectTagSetQuery where(JpaPredicate... conditions) {
        super.where(conditions);
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

    JpaPredicate hasId(TagSetId id) {
        return () -> criteriaBuilder.equal(root.get("id"), id.id().toString());
    }

    JpaPredicate isAccessibleFor(User searcher) {
        return () -> criteriaBuilder.equal(root.get("creatorId"), searcher.id().toString());
    }

    JpaPredicate isNotDeleted() {
        return () -> criteriaBuilder.isFalse(root.get("deleted"));
    }
}
