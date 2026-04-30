package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.jpa.JpaEntity;

import java.util.List;

import static java.util.Arrays.stream;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public abstract class MultiResultJpaQuery<E extends JpaEntity, P> extends JpaQuery<E, P, List<P>> {

    private Integer rowLimit;
    private List<JpaOrderCriteria> sortCriteria;

    protected MultiResultJpaQuery(EntityManager entityManager) {
        super(entityManager);
    }

    // TODO make protected
    protected abstract JpaSortBuilder<E> sort();

    public final MultiResultJpaQuery<E, P> limit(int rowNum) {
        if (rowNum < 0) {
            throw new IllegalArgumentException("Number of rows cannot be less than 0");
        }
        this.rowLimit = rowNum;
        return this;
    }

    public final MultiResultJpaQuery<E, P> orderBy(EntitySortCriteria sortCriteria) {
        this.sortCriteria = sort().toOrderCriteria(sortCriteria);
        return this;
    }

    // TODO remove
    public MultiResultJpaQuery<E, P> orderBy(JpaOrderCriteria... sortCriteria) {
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
