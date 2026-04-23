package ovh.equino.actracker.domain;

import java.util.Deque;
import java.util.LinkedList;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.domain.EntitySortCriteria.CommonField.ID;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.ASC;

public record EntitySortCriteria(
        Deque<Level> levels
) {

    public EntitySortCriteria {
        levels = new LinkedList<>(requireNonNullElse(levels, emptyList()));
        levels.addLast(sortGuard());
    }

    public EntitySortCriteria(Level... levels) {
        this(
                new LinkedList<>(
                        stream(
                                requireNonNullElse(levels, new Level[]{})
                        ).toList()
                )
        );
    }

    public static EntitySortCriteria irrelevant() {
        return new EntitySortCriteria();
    }

    public static EntitySortCriteria sortBy(Field field, Order order) {
        return new EntitySortCriteria(new Level(field, order));
    }

    public record Level(
            Field field,
            Order order
    ) {

        public Level {
            order = requireNonNullElse(order, ASC);
            requireNonNull(field);
        }
    }

    public interface Field {
    }

    public enum CommonField implements Field {
        // TODO disable sort by ID
        ID
    }

    public enum Order {
        ASC,
        DESC
    }

    private Level sortGuard() {
        return new Level(ID, ASC);
    }
}
