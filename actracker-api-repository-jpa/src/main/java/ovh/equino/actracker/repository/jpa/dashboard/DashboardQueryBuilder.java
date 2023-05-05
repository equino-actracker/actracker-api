package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.util.Map;

class DashboardQueryBuilder extends JpaQueryBuilder<DashboardEntity> {

    DashboardQueryBuilder(EntityManager entityManager) {
        super(entityManager, DashboardEntity.class, Map.of());
    }

    Predicate matchesTerm(String term) {
        return super.matchesTerm(term, "name");
    }
}
