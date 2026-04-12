package ovh.equino.actracker.application;

import ovh.equino.actracker.domain.EntitySortCriteria;

import java.util.LinkedHashMap;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.ASC;

public record SortCriteria(LinkedHashMap<String, String> sortFieldWithOrder) {

    public SortCriteria {
        sortFieldWithOrder = requireNonNullElse(sortFieldWithOrder, new LinkedHashMap<>());
    }

    public SortCriteria() {
        this(new LinkedHashMap<>());
    }

    public void orderBy(String field, String order) {
        sortFieldWithOrder.put(field, order);
    }

    public EntitySortCriteria toEntitySortCriteria(FieldResolver sortableFieldResolver) {
        var sortLevels = sortFieldWithOrder.entrySet().stream()
                .map(criterion -> toEntitySortCriteriaLevel(
                        criterion.getKey(),
                        criterion.getValue(),
                        sortableFieldResolver
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(EntitySortCriteria.Level[]::new);
        return new EntitySortCriteria(sortLevels);
    }

    private Optional<EntitySortCriteria.Level> toEntitySortCriteriaLevel(String field,
                                                                         String order,
                                                                         FieldResolver sortableFieldResolver) {

        var sortableField = sortableFieldResolver.fromString(field);
        var formattedOrder = requireNonNullElse(order, ASC.toString()).toUpperCase();
        var sortCriteriaOrder = EntitySortCriteria.Order.valueOf(formattedOrder);
        return sortableField.map(it -> new EntitySortCriteria.Level(it, sortCriteriaOrder));
    }

    public interface FieldResolver {
        Optional<? extends EntitySortCriteria.Field> fromString(String field);

        default Optional<? extends EntitySortCriteria.Field> commonFieldFromString(String field) {
            if (isBlank(field)) {
                return Optional.empty();
            }
            return stream(EntitySortCriteria.CommonField.values())
                    .filter(commonField -> commonField.toString().equals(field.toUpperCase()))
                    .findAny();
        }
    }
}
