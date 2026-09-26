package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

// TODO add missing fields
public record TagTestData(UUID id, TenantTestData creator, String name) {

    public static TagTestData minimalTag() {
        return new TagTestData(randomUUID(), aTenant(), "nameless tag");
    }

    public TagTestData createdBy(TenantTestData creator) {
        return new TagTestData(this.id, creator, this.name);
    }

    public TagTestData withId(UUID id) {
        return new TagTestData(id, this.creator, this.name);
    }

    public TagTestData named(String name) {
        return new TagTestData(this.id, this.creator, name);
    }

    public TagDto asDto() {
        return new TagDto(id, creator.id(), name, emptyList(), emptyList(), false);
    }
}
