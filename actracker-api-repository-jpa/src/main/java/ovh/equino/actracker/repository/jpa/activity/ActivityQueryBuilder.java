package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

class ActivityQueryBuilder extends JpaQueryBuilder<ActivityEntity> {

    ActivityQueryBuilder(EntityManager entityManager) {
        super(entityManager, ActivityEntity.class);
    }
}
