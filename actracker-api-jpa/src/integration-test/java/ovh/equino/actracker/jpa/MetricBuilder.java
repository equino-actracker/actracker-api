package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;
import ovh.equino.actracker.domain.tenant.TenantDto;

import static ovh.equino.actracker.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.jpa.TestUtil.randomString;

public final class MetricBuilder {

    private MetricDto newMetric;

    MetricBuilder(TenantDto creator) {
        this.newMetric = new MetricDto(
                nextUUID(),
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
