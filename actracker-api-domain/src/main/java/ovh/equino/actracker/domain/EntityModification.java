package ovh.equino.actracker.domain;

@FunctionalInterface
public interface EntityModification {

    void execute();
}
