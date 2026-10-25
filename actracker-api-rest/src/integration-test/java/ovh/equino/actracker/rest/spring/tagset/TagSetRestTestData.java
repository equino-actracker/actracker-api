package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.application.tagset.CreateTagSetCommand;
import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.domain.tagset.TagSetTestData;
import ovh.equino.actracker.rest.spring.PayloadUtils;
import ovh.equino.actracker.rest.spring.RestTestData;

import java.util.UUID;

import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

record TagSetRestTestData(TagSetTestData tagSet) implements RestTestData<TagSetResult> {

    static TagSetRestTestData aRestfulTagSet(TagSetTestData tagSet) {
        return new TagSetRestTestData(tagSet);
    }

    String asHttpRequest() {
        return """
                {
                    "name": {name},
                    "tags": {tags}
                }
                """
                .replace("{name}", jsonValue(tagSet.name()))
                .replace("{tags}", jsonValue(tagSet.tags(), PayloadUtils::jsonValue));
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

    CreateTagSetCommand asCreateTagSetCommand() {
        return new CreateTagSetCommand(
                tagSet.name(),
                tagSet.tags()
        );
    }

    public TagSetResult asEntityResult() {
        return new TagSetResult(tagSet.id(), tagSet.name(), tagSet.tags());
    }
}
