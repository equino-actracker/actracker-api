package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public final class TagSetTestData {

    private UUID id = randomUUID();
    private TenantTestData creator = aTenant();
    private String name = "nameless tag set";

    public static TagSetTestData aTagSet() {
        return new TagSetTestData();
    }

    public TagSetTestData createdBy(TenantTestData creator) {
        this.creator = creator;
        return this;
    }

    public TagSetTestData withId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID id() {
        return id;
    }

    public TagSetTestData named(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public TagSetDto asDto() {
        return new TagSetDto(id, creator.id(), name, emptySet(), false);
    }
}
