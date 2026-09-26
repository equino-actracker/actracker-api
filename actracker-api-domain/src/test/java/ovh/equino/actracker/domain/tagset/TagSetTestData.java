package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

// TODO add missing fields
public record TagSetTestData(UUID id, TenantTestData creator, String name) {

    public static TagSetTestData minimalTagSet() {
        return new TagSetTestData(randomUUID(), aTenant(), "nameless tag set");
    }

    public TagSetTestData createdBy(TenantTestData creator) {
        return new TagSetTestData(this.id, creator, this.name);
    }

    public TagSetTestData withId(UUID id) {
        return new TagSetTestData(id, this.creator, this.name);
    }

    public TagSetTestData named(String name) {
        return new TagSetTestData(this.id, this.creator, name);
    }

    public TagSetDto asDto() {
        return new TagSetDto(id, creator.id(), name, emptySet(), false);
    }
}
