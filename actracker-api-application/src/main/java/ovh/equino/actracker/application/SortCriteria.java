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

    public interface FieldResolver {
        Optional<? extends EntitySortCriteria.Field> fromString(String field);
    }

    public static final class Translator {
        private final FieldResolver sortableFieldResolver;

        public Translator(FieldResolver sortableFieldResolver) {
            this.sortableFieldResolver = sortableFieldResolver;
        }

        public EntitySortCriteria toEntitySortCriteria(SortCriteria sortCriteria) {
            var sortLevels = sortCriteria.sortFieldWithOrder().entrySet().stream()
                    .map(criterion -> toEntitySortCriteriaLevel(criterion.getKey(), criterion.getValue()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toArray(EntitySortCriteria.Level[]::new);
            return new EntitySortCriteria(sortLevels);
        }

        private Optional<EntitySortCriteria.Level> toEntitySortCriteriaLevel(String field, String order) {
            var formattedOrder = requireNonNullElse(order, ASC.toString()).toUpperCase();
            var sortCriteriaOrder = EntitySortCriteria.Order.valueOf(formattedOrder);
            return commonFieldFromString(field).map(it -> toSortLevel(it, sortCriteriaOrder)).or(() ->
                    sortableFieldResolver.fromString(field).map(it -> toSortLevel(it, sortCriteriaOrder))
            );
        }

        private static EntitySortCriteria.Level toSortLevel(EntitySortCriteria.Field it,
                                                            EntitySortCriteria.Order sortCriteriaOrder) {

            return new EntitySortCriteria.Level(it, sortCriteriaOrder);
        }

        private Optional<? extends EntitySortCriteria.Field> commonFieldFromString(String field) {
            if (isBlank(field)) {
                return Optional.empty();
            }
            return stream(EntitySortCriteria.CommonField.values())
                    .filter(commonField -> commonField.toString().equals(field.toUpperCase()))
                    .findAny();
        }
    }
}
