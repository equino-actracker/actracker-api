package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

abstract class JpaQuery<E, P, R> {

    protected final EntityManager entityManager;
    protected final CriteriaBuilder criteriaBuilder;
    protected final Root<E> root;
    protected final CriteriaQuery<P> query;

    protected JpaPredicate predicate;

    JpaQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery(getProjectionType());
        this.root = query.from(getRootEntityType());
    }

    protected abstract void initProjection();

    public abstract JpaPredicateBuilder<E> predicate();

    public JpaQuery<E, P, R> where(JpaPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

    public abstract R execute();

    protected abstract Class<E> getRootEntityType();

    protected abstract Class<P> getProjectionType();
}
