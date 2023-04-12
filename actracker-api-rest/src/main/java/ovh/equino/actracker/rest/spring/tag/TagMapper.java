package ovh.equino.actracker.rest.spring.tag;


import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchResult;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.List;

class TagMapper extends PayloadMapper {

    TagDto fromRequest(Tag tagRequest) {
        return new TagDto(tagRequest.name());
    }

    Tag toResponse(TagDto tag) {
        return new Tag(uuidToString(tag.id()), tag.name());
    }

    List<Tag> toResponse(List<TagDto> tags) {
        return tags.stream()
                .map(this::toResponse)
                .toList();
    }

    TagSearchResponse toResponse(TagSearchResult tagSearchResult) {
        List<Tag> foundTags = toResponse(tagSearchResult.tags());
        return new TagSearchResponse(tagSearchResult.nextPageId(), foundTags);
    }

}
