package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.Order;

// TODO should be removed?
public interface JpaSortCriteria {

    Order toRawSort();
}
