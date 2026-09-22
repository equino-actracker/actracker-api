package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.domain.tagset.TagSetTestData;

record TagSetRestTestData(TagSetTestData tagSetTestData) {

    static TagSetRestTestData aRestfulTagSet(TagSetTestData tagSetTestData) {
        return new TagSetRestTestData(tagSetTestData);
    }
}
