package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.util.Map;

class TagQueryBuilder extends JpaQueryBuilder<TagEntity> {

    TagQueryBuilder(EntityManager entityManager) {
        super(entityManager, TagEntity.class, Map.of());
    }

    @Override
    public Predicate isAccessibleFor(User searcher) {
        return or(
                super.isAccessibleFor(searcher),
                isGrantee(searcher)
        );
    }

    private Predicate isGrantee(User user) {
        Join<TagEntity, TagShareEntity> sharedTag = rootEntity.join("shares", JoinType.LEFT);
        Subquery<Long> subQuery = criteriaQuery.subquery(Long.class);
        subQuery.select(criteriaBuilder.literal(1L))
                .where(criteriaBuilder.equal(sharedTag.get("granteeId"), user.id().toString()))
                .from(TagEntity.class);
        return criteriaBuilder.exists(subQuery);
    }

    Predicate matchesTerm(String term) {
        return super.matchesTerm(term, "name");
    }
}
