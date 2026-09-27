package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.domain.tagset.TagSetTestData;

record TagSetRestTestData(TagSetTestData tagSet) {

    static TagSetRestTestData aRestfulTagSet(TagSetTestData tagSet) {
        return new TagSetRestTestData(tagSet);
    }

    TagSetResult asTagSetResult() {
        return new TagSetResult(tagSet.id(), tagSet.name(), tagSet.tags());
    }

    public String asHttpResponse() {
        return "";
    }
}
