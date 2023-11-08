package ovh.equino.actracker.repository.jpa;

@FunctionalInterface
public interface TransactionalOperation {

    void execute();
}
