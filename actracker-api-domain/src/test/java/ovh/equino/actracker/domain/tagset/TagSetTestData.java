package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public record TagSetTestData(UUID id, TenantTestData creator, String name, Set<UUID> tags, boolean isDeleted) {

    public static TagSetTestData minimalTagSet() {
        return new TagSetTestData(randomUUID(), aTenant(), "nameless tag set", emptySet(), false);
    }

    public TagSetTestData createdBy(TenantTestData creator) {
        return new TagSetTestData(this.id, creator, this.name, this.tags, this.isDeleted);
    }

    public TagSetTestData withId(UUID id) {
        return new TagSetTestData(id, this.creator, this.name, this.tags, this.isDeleted);
    }

    public TagSetTestData named(String name) {
        return new TagSetTestData(this.id, this.creator, name, this.tags, this.isDeleted);
    }

    public TagSetDto asDto() {
        return new TagSetDto(id, creator.id(), name, tags, isDeleted);
    }
}
