package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;

import java.util.Optional;

public abstract class SingleResultJpaQuery<E, P> extends JpaQuery<E, P, Optional<P>> {

    protected SingleResultJpaQuery(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public final Optional<P> execute() {
        initProjection();
        if (predicate != null) {
            query.where(predicate.toRawPredicate());
        }
        return entityManager.createQuery(query)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
