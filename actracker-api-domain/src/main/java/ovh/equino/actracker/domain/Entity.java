package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.user.User;

public interface Entity {

    User creator();

    default boolean isEditableFor(User user) {
        return user.equals(creator());
    }

    default void validate() {}
}
