package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.jpa.JpaEntity;
import ovh.equino.actracker.jpa.JpaEntity_;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;

public abstract class JpaOrderBuilder<E extends JpaEntity> {

    private final CriteriaBuilder criteriaBuilder;
    private final Root<E> root;

    protected JpaOrderBuilder(CriteriaBuilder criteriaBuilder, Root<E> root) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
    }

    // TODO remove
    public JpaOrderCriteria ascending(String fieldName) {
        return () -> criteriaBuilder.asc(root.get(fieldName));
    }

    // TODO make package private
    public List<JpaOrderCriteria> from(EntitySortCriteria sortCriteria) {
        return sortCriteria.levels().stream()
                .map(this::toOrderCriteria)
                .flatMap(List::stream)
                .toList();
    }

    private List<JpaOrderCriteria> toOrderCriteria(EntitySortCriteria.Level sortCriterion) {
        var commonOrderCriteria = toCommonOrderCriteria(sortCriterion);
        if (isNotEmpty(commonOrderCriteria)) {
            return commonOrderCriteria;
        }
        return toEntityOrderCriteria(sortCriterion);
    }

    private List<JpaOrderCriteria> toCommonOrderCriteria(EntitySortCriteria.Level sortCriterion) {
        if (sortCriterion.field() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> DESC == sortCriterion.order()
                        ? singletonList(() -> criteriaBuilder.desc(root.get(JpaEntity_.id)))
                        : singletonList(() -> criteriaBuilder.asc(root.get(JpaEntity_.id)));
            };
        }
        return emptyList();
    }

    protected abstract List<JpaOrderCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion);
}
