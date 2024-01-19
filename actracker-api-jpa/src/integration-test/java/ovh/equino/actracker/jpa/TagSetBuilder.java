package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.jpa.TestUtil.randomString;

public final class TagSetBuilder {

    private TagSetDto newTagSet;

    TagSetBuilder(TenantDto creator) {
        this.newTagSet = new TagSetDto(
                nextUUID(),
                creator.id(),
                randomString(),
                Set.of(nextUUID(), nextUUID(), nextUUID()),
                false
        );
    }

    public TagSetBuilder named(String name) {
        this.newTagSet = new TagSetDto(
                newTagSet.id(),
                newTagSet.creatorId(),
                name,
                newTagSet.tags(),
                newTagSet.deleted()
        );
        return this;
    }

    public TagSetBuilder deleted() {
        this.newTagSet = new TagSetDto(
                newTagSet.id(),
                newTagSet.creatorId(),
                newTagSet.name(),
                newTagSet.tags(),
                true
        );

        return this;
    }

    public TagSetBuilder withTags(TagDto... tags) {
        this.newTagSet = new TagSetDto(
                newTagSet.id(),
                newTagSet.creatorId(),
                newTagSet.name(),
                stream(tags).map(TagDto::id).collect(toUnmodifiableSet()),
                newTagSet.deleted()
        );

        return this;
    }

    public TagSetDto build() {
        return newTagSet;
    }
}
