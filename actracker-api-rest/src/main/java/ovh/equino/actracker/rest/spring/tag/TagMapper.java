package ovh.equino.actracker.rest.spring.tag;


import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.share.ShareMapper;

import java.util.List;

class TagMapper extends PayloadMapper {

    private final MetricMapper metricMapper = new MetricMapper();
    private final ShareMapper shareMapper = new ShareMapper();

    TagDto fromRequest(Tag tagRequest) {
        return new TagDto(
                tagRequest.name(),
                metricMapper.fromRequest(tagRequest.metrics()),
                shareMapper.fromRequest(tagRequest.shares())
        );
    }

    Tag toResponse(TagDto tag) {
        return new Tag(
                uuidToString(tag.id()),
                tag.name(),
                metricMapper.toResponse(tag.metrics()),
                shareMapper.toResponse(tag.shares())
        );
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
