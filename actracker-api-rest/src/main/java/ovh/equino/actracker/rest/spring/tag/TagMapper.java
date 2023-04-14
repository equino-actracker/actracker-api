package ovh.equino.actracker.rest.spring.tag;


import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.split;

class TagMapper extends PayloadMapper {

    TagDto fromRequest(Tag tagRequest) {
        return new TagDto(tagRequest.name());
    }

    EntitySearchCriteria fromRequest(User requester, String pageId, Integer pageSize, String term, String excludedTags) {
        return new EntitySearchCriteria(requester, pageSize, pageId, term, parseIds(excludedTags));
    }

    Tag toResponse(TagDto tag) {
        return new Tag(uuidToString(tag.id()), tag.name());
    }

    List<Tag> toResponse(List<TagDto> tags) {
        return tags.stream()
                .map(this::toResponse)
                .toList();
    }

    TagSearchResponse toResponse(EntitySearchResult<TagDto> tagSearchResult) {
        List<Tag> foundTags = toResponse(tagSearchResult.results());
        return new TagSearchResponse(tagSearchResult.nextPageId(), foundTags);
    }

    Set<UUID> parseIds(String jointIds) {
        String[] parsedIds = requireNonNullElse(split(jointIds, ','), new String[]{});

        return stream(parsedIds)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
    }
}
