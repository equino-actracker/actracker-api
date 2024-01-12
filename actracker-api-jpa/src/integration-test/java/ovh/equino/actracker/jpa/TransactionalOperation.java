package ovh.equino.actracker.jpa;

@FunctionalInterface
public interface TransactionalOperation {

    void execute();
}
