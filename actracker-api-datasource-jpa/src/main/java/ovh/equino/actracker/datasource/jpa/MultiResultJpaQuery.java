package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Order;

import java.util.List;

import static java.util.Arrays.stream;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public abstract class MultiResultJpaQuery<E, P> extends JpaQuery<E, P, List<P>> {

    private Integer rowLimit;
    private List<JpaSortCriteria> sortCriteria;

    protected MultiResultJpaQuery(EntityManager entityManager) {
        super(entityManager);
    }

    public abstract JpaSortBuilder<E> sort();

    public final MultiResultJpaQuery<E, P> limit(int rowNum) {
        if (rowNum < 0) {
            throw new IllegalArgumentException("Number of rows cannot be less than 0");
        }
        this.rowLimit = rowNum;
        return this;
    }

    public final MultiResultJpaQuery<E, P> orderBy(JpaSortCriteria... sortCriteria) {
        if (sortCriteria != null) {
            this.sortCriteria = stream(sortCriteria).toList();
        }
        return this;
    }

    @Override
    public final List<P> execute() {
        initProjection();
        if (predicate != null) {
            query.where(predicate.toRawPredicate());
        }
        if (isNotEmpty(sortCriteria)) {
            List<Order> order = sortCriteria
                    .stream()
                    .map(JpaSortCriteria::toRawSort)
                    .toList();
            query.orderBy(order);
        }
        TypedQuery<P> typedQuery = entityManager.createQuery(query);
        if (rowLimit != null) {
            typedQuery.setMaxResults(rowLimit);
        }
        return typedQuery.getResultList();
    }
}
