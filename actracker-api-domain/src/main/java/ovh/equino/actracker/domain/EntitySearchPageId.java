package ovh.equino.actracker.domain;

import javax.swing.*;
import java.util.Deque;
import java.util.LinkedList;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.ASC;

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

    public record Value(EntitySortCriteria.Field sortField, EntitySortCriteria.Order sortOrder, Object value) {

        public Value {
            sortOrder = requireNonNullElse(sortOrder, ASC);
            requireNonNull(sortField);
        }

        public static Value of(EntitySortCriteria.Field sortField, EntitySortCriteria.Order sortOrder, Object value) {
            return new Value(sortField, sortOrder, value);
        }
    }
}
