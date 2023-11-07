package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class TagSetBuilder {

    private TagSetDto newTagSet;

    TagSetBuilder(TenantDto creator) {
        this.newTagSet = new TagSetDto(
                randomUUID(),
                creator.id(),
                randomString(),
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                false
        );
    }

    public TagSetBuilder deleted() {
        this.newTagSet = new TagSetDto(
                newTagSet.id(),
                newTagSet.creatorId(),
                newTagSet.name(),
                newTagSet.tags(),
                newTagSet.deleted()
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
