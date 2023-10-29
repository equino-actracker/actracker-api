package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public abstract class MultiResultJpaQuery<E, P> extends JpaQuery<E, P, List<P>> {

    private Integer rowLimit;

    protected MultiResultJpaQuery(EntityManager entityManager) {
        super(entityManager);
    }

    public final MultiResultJpaQuery<E, P> limit(int rowNum) {
        if (rowNum < 0) {
            throw new IllegalArgumentException("Number of rows cannot be less than 0");
        }
        this.rowLimit = rowNum;
        return this;
    }

    @Override
    public final List<P> execute() {
        initQuery();
        if (predicate != null) {
            query.where(predicate.toRawPredicate());
        }
        TypedQuery<P> typedQuery = entityManager.createQuery(query);
        if (rowLimit != null) {
            typedQuery.setMaxResults(rowLimit);
        }
        return typedQuery.getResultList();
    }
}
