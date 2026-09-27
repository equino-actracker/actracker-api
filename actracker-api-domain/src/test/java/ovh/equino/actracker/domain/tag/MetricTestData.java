package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public record MetricTestData(UUID id, TenantTestData creator, String name, MetricType type, boolean isDeleted) {

    public static MetricTestData aMetric() {
        return new MetricTestData(randomUUID(), aTenant(), "nameless metric", MetricType.NUMERIC, false);
    }

    public MetricDto asDto() {
        return new MetricDto(id, creator.id(), name, type, isDeleted);
    }
}
