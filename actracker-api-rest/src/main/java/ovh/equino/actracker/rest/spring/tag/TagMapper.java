package ovh.equino.actracker.rest.spring.tag;


import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchResult;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

class TagMapper extends PayloadMapper {

    TagDto fromRequest(Tag tagRequest) {
        return new TagDto(tagRequest.name());
    }

    TagSearchCriteria fromRequest(User requester, String pageId, Integer pageSize, String term, String excludedTags) {
        String[] excluded = requireNonNullElse(split(excludedTags, ','), new String[]{});

        Set<UUID> excludedTagIds = stream(excluded)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        return new TagSearchCriteria(requester, pageSize, pageId, term, excludedTagIds);
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
