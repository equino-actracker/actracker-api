package ovh.equino.actracker.rest.spring.tag;


import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;

import java.util.List;

class TagMapper extends PayloadMapper {

    TagDto fromRequest(Tag tagRequest) {
        return new TagDto(tagRequest.name());
    }

    Tag toResponse(TagDto tag) {
        return new Tag(uuidToString(tag.id()), tag.name());
    }

    SearchResponse<Tag> toResponse(EntitySearchResult<TagDto> tagSearchResult) {
        List<Tag> foundTags = toResponse(tagSearchResult.results());
        return new SearchResponse<>(tagSearchResult.nextPageId(), foundTags);
    }

    List<Tag> toResponse(List<TagDto> tags) {
        return tags.stream()
                .map(this::toResponse)
                .toList();
    }

}
