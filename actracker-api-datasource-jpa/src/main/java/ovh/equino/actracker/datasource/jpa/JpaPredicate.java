package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.Predicate;

public interface JpaPredicate {

    Predicate toRawPredicate();
}
