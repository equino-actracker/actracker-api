package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.jpa.JpaEntity;

import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public abstract class MultiResultJpaQuery<E extends JpaEntity, P> extends JpaQuery<E, P, List<P>> {

    private Integer rowLimit;
    private List<JpaOrderCriteria> sortCriteria;

    protected MultiResultJpaQuery(EntityManager entityManager) {
        super(entityManager);
    }

    public abstract JpaOrderBuilder<E> order();

    public final MultiResultJpaQuery<E, P> limit(int rowNum) {
        if (rowNum < 0) {
            throw new IllegalArgumentException("Number of rows cannot be less than 0");
        }
        this.rowLimit = rowNum;
        return this;
    }

    public MultiResultJpaQuery<E, P> orderBy(List<JpaOrderCriteria> sortCriteria) {
        if (isNotEmpty(sortCriteria)) {
            this.sortCriteria = sortCriteria;
        }
        return this;
    }

    public MultiResultJpaQuery<E, P> orderBy(JpaOrderCriteria... sortCriteria) {
        return orderBy(List.of(requireNonNullElse(sortCriteria, new JpaOrderCriteria[0])));
    }

    @Override
    public final List<P> execute() {
        initProjection();
        if (predicate != null) {
            query.where(predicate.toRawPredicate());
        }
        if (isNotEmpty(sortCriteria)) {
            var order = sortCriteria
                    .stream()
                    .map(JpaOrderCriteria::toRawOrder)
                    .toList();
            query.orderBy(order);
        }
        var typedQuery = entityManager.createQuery(query);
        if (rowLimit != null) {
            typedQuery.setMaxResults(rowLimit);
        }
        return typedQuery.getResultList();
    }
}
