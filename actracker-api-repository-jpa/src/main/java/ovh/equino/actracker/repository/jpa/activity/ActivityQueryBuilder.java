package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.time.Instant;

class ActivityQueryBuilder extends JpaQueryBuilder<ActivityEntity> {

    ActivityQueryBuilder(EntityManager entityManager) {
        super(entityManager, ActivityEntity.class);
    }

    Predicate isInTimeRange(Instant timeRangeStart, Instant timeRangeEnd) {
        Predicate isBeforeRangeEnd = timeRangeEnd != null
                ? isStartedBeforeOrAt(timeRangeEnd)
                : allMatch();

        Predicate isAfterRangeStart = timeRangeStart != null
                //@formatter:off
                ? and(
                    isStarted(),
                    or(
                        isNotFinished(),
                        isFinishedAfterOrAt(timeRangeStart)
                    )
                )
                : allMatch();
                //@formatter:on

        return and(isBeforeRangeEnd, isAfterRangeStart);
    }

    Predicate isStartedBeforeOrAt(Instant startTime) {
        return and(
                isStarted(),
                criteriaBuilder.lessThanOrEqualTo(
                        rootEntity.get("startTime"),
                        startTime
                )
        );
    }

    Predicate isFinishedAfterOrAt(Instant endTime) {
        return and(
                isFinished(),
                criteriaBuilder.greaterThanOrEqualTo(
                        rootEntity.get("endTime"),
                        endTime
                )
        );
    }

    Predicate isStarted() {
        return criteriaBuilder.isNotNull(
                rootEntity.get("startTime")
        );
    }

    Predicate isFinished() {
        return criteriaBuilder.isNotNull(
                rootEntity.get("endTime")
        );
    }

    Predicate isNotFinished() {
        return criteriaBuilder.isNull(
                rootEntity.get("endTime")
        );
    }

}
