package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.criteria.Order;

public interface JpaSortCriteria {

    Order toRawSort();
}
