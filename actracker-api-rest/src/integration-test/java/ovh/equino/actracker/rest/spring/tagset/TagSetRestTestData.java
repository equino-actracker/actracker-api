package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.domain.tagset.TagSetTestData;

record TagSetRestTestData(TagSetTestData tagSetTestData) {

    static TagSetRestTestData aRestfulTagSet(TagSetTestData tagSetTestData) {
        return new TagSetRestTestData(tagSetTestData);
    }

    TagSetResult asTagSetResult() {
        return null;
    }

    public String asHttpResponse() {
        return "";
    }
}
