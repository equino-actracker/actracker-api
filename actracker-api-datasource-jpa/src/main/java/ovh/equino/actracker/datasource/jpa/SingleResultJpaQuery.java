package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.jpa.JpaEntity;

import java.util.Optional;

public abstract class SingleResultJpaQuery<E extends JpaEntity, P> extends JpaQuery<E, P, Optional<P>> {

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
