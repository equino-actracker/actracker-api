package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.time.Instant;

class ActivityQueryBuilder extends JpaQueryBuilder<ActivityEntity> {

    ActivityQueryBuilder(EntityManager entityManager) {
        super(entityManager, ActivityEntity.class);
    }

    Predicate isStartedBefore(Instant startTime) {
        return and(
                isStarted(),
                criteriaBuilder.lessThan(
                        rootEntity.get("startTime"),
                        startTime
                )
        );
    }

    Predicate isStarted() {
        return criteriaBuilder.isNotNull(
                rootEntity.get("startTime")
        );
    }

    Predicate isNotFinished() {
        return criteriaBuilder.isNull(
                rootEntity.get("endTime")
        );
    }
}
