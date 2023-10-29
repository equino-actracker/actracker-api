package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static java.util.Arrays.stream;

abstract class JpaQuery<E, P, R> {

    protected final EntityManager entityManager;
    protected final CriteriaBuilder criteriaBuilder;
    protected final Root<E> root;
    protected final CriteriaQuery<P> query;

    JpaQuery(EntityManager entityManager) {

        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery(getProjectionType());
        this.root = query.from(getRootEntityType());
    }

    public void where(JpaPredicate... conditions) {
        Predicate[] predicates = stream(conditions)
                .map(JpaPredicate::toJpa)
                .toArray(Predicate[]::new);
        query.where(predicates);
    }

    public abstract R execute();

    protected abstract Class<E> getRootEntityType();

    protected abstract Class<P> getProjectionType();
}
