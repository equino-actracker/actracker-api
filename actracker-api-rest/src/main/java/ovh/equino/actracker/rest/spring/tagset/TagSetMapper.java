package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;

import java.util.List;

class TagSetMapper extends PayloadMapper {

    TagSetDto fromRequest(TagSet tagSetRequest) {
        return new TagSetDto(
                tagSetRequest.name(),
                stringsToUuids(tagSetRequest.tags())
        );
    }

    TagSet toResponse(TagSetDto tagSet) {
        return new TagSet(
                uuidToString(tagSet.id()),
                tagSet.name(),
                uuidsToStrings(tagSet.tags())
        );
    }

    SearchResponse<TagSet> toResponse(EntitySearchResult<TagSetDto> tagSetSearchResult) {
        List<TagSet> foundTagSets = toResponse(tagSetSearchResult.results());
        return new SearchResponse<>(tagSetSearchResult.nextPageId(), foundTagSets);
    }

    private List<TagSet> toResponse(List<TagSetDto> tagSets) {
        return tagSets.stream()
                .map(this::toResponse)
                .toList();
    }
}
