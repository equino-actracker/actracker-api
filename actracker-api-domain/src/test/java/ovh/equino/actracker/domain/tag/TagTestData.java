package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public record TagTestData(UUID id,
                          TenantTestData creator,
                          String name,
                          List<MetricTestData> metrics,
                          List<Share> shares,
                          boolean isDeleted) {

    public static TagTestData minimalTag() {
        return new TagTestData(randomUUID(), aTenant(), "nameless tag", emptyList(), emptyList(), false);
    }

    public TagTestData createdBy(TenantTestData creator) {
        return new TagTestData(this.id, creator, this.name, this.metrics, this.shares, this.isDeleted);
    }

    public TagTestData withId(UUID id) {
        return new TagTestData(id, this.creator, this.name, this.metrics, this.shares, this.isDeleted);
    }

    public TagTestData named(String name) {
        return new TagTestData(this.id, this.creator, name, this.metrics, this.shares, this.isDeleted);
    }

    public TagDto asDto() {
        var metricDtos = metrics.stream().map(MetricTestData::asDto).toList();
        return new TagDto(id, creator.id(), name, metricDtos, shares, this.isDeleted);
    }
}
