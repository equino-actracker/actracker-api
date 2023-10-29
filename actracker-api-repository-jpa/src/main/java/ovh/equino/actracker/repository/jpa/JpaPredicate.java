package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.criteria.Predicate;

public interface JpaPredicate {

    Predicate toJpa();
}
