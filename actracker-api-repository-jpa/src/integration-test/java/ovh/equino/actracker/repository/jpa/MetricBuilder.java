package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;
import ovh.equino.actracker.domain.tenant.TenantDto;

import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class MetricBuilder {

    private MetricDto newMetric;

    MetricBuilder(TenantDto creator) {
        this.newMetric = new MetricDto(
                randomUUID(),
                creator.id(),
                randomString(),
                MetricType.NUMERIC,
                false
        );
    }

    public MetricBuilder deleted() {
        this.newMetric = new MetricDto(
                newMetric.id(),
                newMetric.creatorId(),
                newMetric.name(),
                newMetric.type(),
                true
        );
        return this;
    }

    public MetricDto build() {
        return newMetric;
    }
}
