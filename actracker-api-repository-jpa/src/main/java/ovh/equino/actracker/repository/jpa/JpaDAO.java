package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Transactional
public abstract class JpaDAO {

    protected final EntityManager entityManager;

    protected JpaDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
