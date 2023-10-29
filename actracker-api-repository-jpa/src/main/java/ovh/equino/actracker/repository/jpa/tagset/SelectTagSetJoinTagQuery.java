package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

import java.util.List;

import static jakarta.persistence.criteria.JoinType.INNER;

class SelectTagSetJoinTagQuery extends MultiResultJpaQuery<TagSetEntity, TagSetJoinTagProjection> {

    private final Join<TagSetEntity, ?> tag;

    SelectTagSetJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tag = root.join("tags", INNER);

        query.select(
                criteriaBuilder.construct(
                        TagSetJoinTagProjection.class,
                        tag.get("id"),
                        root.get("id")
                )
        );
    }

    @Override
    public List<TagSetJoinTagProjection> execute() {
        return entityManager.createQuery(query).getResultList();
    }

    public JpaPredicate assignedForTagSet(TagSetId tagSetId) {
        return () -> criteriaBuilder.equal(root.get("id"), tagSetId.id().toString());
    }

    public JpaPredicate isNotDeleted() {
        return () -> criteriaBuilder.and(
                criteriaBuilder.isFalse(root.get("deleted")),
                criteriaBuilder.isFalse(tag.get("deleted"))
        );
    }

    public JpaPredicate isAccessibleFor(User user) {
        return () -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("creatorId"), user.id().toString()),
                criteriaBuilder.equal(tag.get("creatorId"), user.id().toString())
        );
    }

    @Override
    protected Class<TagSetEntity> getRootEntityType() {
        return TagSetEntity.class;
    }

    @Override
    protected Class<TagSetJoinTagProjection> getProjectionType() {
        return TagSetJoinTagProjection.class;
    }
}
