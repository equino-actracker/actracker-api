package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.domain.tagset.TagSetTestData;

import java.util.UUID;

import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

record TagSetRestTestData(TagSetTestData tagSet) {

    static TagSetRestTestData aRestfulTagSet(TagSetTestData tagSet) {
        return new TagSetRestTestData(tagSet);
    }

    TagSetResult asTagSetResult() {
        return new TagSetResult(tagSet.id(), tagSet.name(), tagSet.tags());
    }

    public String asHttpResponse() {
        return """
                {
                    "id": {id},
                    "name": {name},
                    "tags": {tags}
                }
                """
                .replace("{id}", jsonValue(tagSet.id()))
                .replace("{name}", jsonValue(tagSet.name()))
                .replace("{tags}", jsonValue(tagSet.tags(), UUID::toString));
    }
}
