package ovh.equino.actracker.domain.exception;

public class InvalidSortFieldException extends RuntimeException {

    public InvalidSortFieldException(String sortField) {
        super("Invalid sort field: %s".formatted(sortField));
    }
}
