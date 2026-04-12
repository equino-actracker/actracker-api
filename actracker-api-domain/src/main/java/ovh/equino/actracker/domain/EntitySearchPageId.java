package ovh.equino.actracker.domain;

import java.util.Deque;
import java.util.LinkedList;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record EntitySearchPageId(Deque<Value> values) {

    public EntitySearchPageId {
        values = requireNonNullElse(values, new LinkedList<>());
    }

    public static EntitySearchPageId firstPage() {
        return aPageId();
    }

    public static EntitySearchPageId aPageId() {
        return new EntitySearchPageId(new LinkedList<>());
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public EntitySearchPageId with(Value value) {
        var newValues = new LinkedList<>(values);
        newValues.add(value);
        return new EntitySearchPageId(newValues);
    }

    @Override
    public Deque<Value> values() {
        return new LinkedList<>(values);
    }

    public record Value(EntitySortCriteria.Level sortLevel, Object value) {

        public Value {
            requireNonNull(sortLevel);
        }

        public static Value of(EntitySortCriteria.Level sortLevel, Object value) {
            return new Value(sortLevel, value);
        }

        public EntitySortCriteria.Field sortField() {
            return sortLevel().field();
        }

        public EntitySortCriteria.Order sortOrder() {
            return sortLevel().order();
        }
    }
}
