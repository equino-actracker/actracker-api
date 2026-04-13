package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;

public abstract class JpaSortBuilder<E> {

    private final CriteriaBuilder criteriaBuilder;
    private final Root<E> root;

    protected JpaSortBuilder(CriteriaBuilder criteriaBuilder, Root<E> root) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
    }

    public JpaSortCriteria ascending(String fieldName) {
        return () -> criteriaBuilder.asc(root.get(fieldName));
    }

    public JpaSortCriteria descending(String fieldName) {
        return () -> criteriaBuilder.desc(root.get(fieldName));
    }
}
