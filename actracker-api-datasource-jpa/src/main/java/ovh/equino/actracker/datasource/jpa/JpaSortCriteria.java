package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.Order;

public interface JpaSortCriteria {

    Order toRawSort();
}
