package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
public abstract class JpaDAO {

    @PersistenceContext
    protected EntityManager entityManager;
}
