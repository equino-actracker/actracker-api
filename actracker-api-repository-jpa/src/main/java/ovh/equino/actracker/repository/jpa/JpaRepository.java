package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
public abstract class JpaRepository {

    @PersistenceContext
    protected EntityManager entityManager;
}
