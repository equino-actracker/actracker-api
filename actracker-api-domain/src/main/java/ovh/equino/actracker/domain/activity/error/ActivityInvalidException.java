package ovh.equino.actracker.domain.activity.error;

import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ActivityInvalidException extends RuntimeException {

    private final List<String> errors;

    public ActivityInvalidException(List<String> errors) {
        this.errors = unmodifiableList(errors);
    }
}
