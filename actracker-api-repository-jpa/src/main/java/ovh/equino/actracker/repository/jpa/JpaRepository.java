package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
public abstract class JpaRepository {

    @PersistenceContext
    protected EntityManager entityManager;

    protected <ENTITY> JpaQueryBuilder<ENTITY> queryBuilder(Class<ENTITY> entityType) {
        return new JpaQueryBuilder<>(entityManager, entityType);
    }

}
