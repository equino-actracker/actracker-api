package ovh.equino.actracker.application;

import ovh.equino.actracker.domain.EntitySortCriteria;

import java.util.LinkedHashMap;

import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.ASC;

public record SortCriteria(LinkedHashMap<String, String> sortFieldWithOrder) {

    public SortCriteria {
        sortFieldWithOrder = requireNonNullElse(sortFieldWithOrder, new LinkedHashMap<>());
    }

    public SortCriteria() {
        this(new LinkedHashMap<>());
    }

    public void orderBy(String field, String order) {
        sortFieldWithOrder.putLast(field, order);
    }

    public EntitySortCriteria toEntitySortCriteria() {
        var sortLevels = sortFieldWithOrder.entrySet().stream()
                .map(criterion -> toEntitySortCriteriaLevel(
                        criterion.getKey(),
                        criterion.getValue()
                ))
                .toArray(EntitySortCriteria.Level[]::new);
        return new EntitySortCriteria(sortLevels);
    }

    private static EntitySortCriteria.Level toEntitySortCriteriaLevel(String field, String order) {
        var formattedOrder = requireNonNullElse(order, ASC.toString()).toUpperCase();
        var sortOrder = EntitySortCriteria.Order.valueOf(formattedOrder);
        return new EntitySortCriteria.Level(field, sortOrder);
    }
}
