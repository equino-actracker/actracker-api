package ovh.equino.actracker.rest.spring.tag;

import ovh.equino.actracker.application.tag.TagResult;
import ovh.equino.actracker.domain.tag.TagTestData;

record TagRestTestData(TagTestData tag) {

    static TagRestTestData aRestfulTag(TagTestData tag) {
        return new TagRestTestData(tag);
    }

    TagResult asTagResult() {
        return null;
    }

    public String asHttpResponse() {
        return "";
    }
}
