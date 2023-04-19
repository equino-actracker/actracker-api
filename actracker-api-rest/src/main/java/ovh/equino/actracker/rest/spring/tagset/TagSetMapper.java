package ovh.equino.actracker.rest.spring.tagset;

import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class TagSetMapper extends PayloadMapper {

    TagSetDto fromRequest(TagSet tagSetRequest) {

        Set<UUID> tagIds = requireNonNullElse(tagSetRequest.tags(), new HashSet<String>()).stream()
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        return new TagSetDto(
                tagSetRequest.name(),
                tagIds
        );
    }

    TagSet toResponse(TagSetDto tagSet) {

        Set<String> tagIds = tagSet.tags().stream()
                .map(super::uuidToString)
                .collect(toUnmodifiableSet());

        return new TagSet(
                uuidToString(tagSet.id()),
                tagSet.name(),
                tagIds
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
