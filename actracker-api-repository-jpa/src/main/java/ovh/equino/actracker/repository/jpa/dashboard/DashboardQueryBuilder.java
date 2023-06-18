package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.util.Map;

class DashboardQueryBuilder extends JpaQueryBuilder<DashboardEntity> {

    DashboardQueryBuilder(EntityManager entityManager) {
        super(entityManager, DashboardEntity.class, Map.of());
    }

    @Override
    public Predicate isAccessibleFor(User searcher) {
        Join<DashboardEntity, DashboardShareEntity> sharedDashboard = rootEntity.join("shares", JoinType.LEFT);
        return or(
                super.isAccessibleFor(searcher),
                isGrantee(searcher, sharedDashboard)
        );
    }

    private Predicate isGrantee(User user, Join<DashboardEntity, DashboardShareEntity> sharedDashboard) {
        Subquery<Long> subQuery = criteriaQuery.subquery(Long.class);
        subQuery.select(criteriaBuilder.literal(1L))
                .where(criteriaBuilder.equal(sharedDashboard.get("granteeId"), user.id().toString()))
                .from(DashboardEntity.class);
        return criteriaBuilder.exists(subQuery);
    }

    Predicate matchesTerm(String term) {
        return super.matchesTerm(term, "name");
    }
}
