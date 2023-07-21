package ovh.equino.actracker.rest.spring.tag;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.tag.SearchTagsQuery;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.share.Share;
import ovh.equino.actracker.rest.spring.share.ShareMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tag")
class TagController {

    private final TagApplicationService tagApplicationService;

    private final TagMapper tagMapper = new TagMapper();
    private final MetricMapper metricMapper = new MetricMapper();
    private final ShareMapper shareMapper = new ShareMapper();

    TagController(TagApplicationService tagApplicationService) {
        this.tagApplicationService = tagApplicationService;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Tag createTag(@RequestBody Tag tag) {
        TagDto tagDto = tagMapper.fromRequest(tag);
        TagDto createdTag = tagApplicationService.createTag(tagDto);

        return tagMapper.toResponse(createdTag);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    List<Tag> resolveTags(@RequestParam(name = "ids", required = false) String tagIds) {
        Set<UUID> parsedIds = tagMapper.parseIds(tagIds);
        List<TagDto> resolvedTags = tagApplicationService.resolveTags(parsedIds);
        return tagMapper.toResponse(resolvedTags);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Tag> searchTags(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "excludedTags", required = false) String excludedTags) {

        SearchTagsQuery searchTagsQuery = new SearchTagsQuery(
                pageSize,
                pageId,
                term,
                tagMapper.parseIds(excludedTags)
        );

        EntitySearchResult<TagDto> searchResult = tagApplicationService.searchTags(searchTagsQuery);
        return tagMapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    void deleteTag(@PathVariable("id") String id) {
        tagApplicationService.deleteTag(UUID.fromString(id));
    }

    @RequestMapping(method = POST, path = "/{id}/share")
    @ResponseStatus(OK)
    Tag shareTag(@PathVariable("id") String id,
                 @RequestBody Share share) {

        ovh.equino.actracker.domain.share.Share newShare = shareMapper.fromRequest(share);

        TagDto sharedTag = tagApplicationService.shareTag(newShare, UUID.fromString(id));
        return tagMapper.toResponse(sharedTag);
    }

    @RequestMapping(method = DELETE, path = "/{id}/share/{granteeName}")
    @ResponseStatus(OK)
    Tag unshareTag(@PathVariable("id") String tagId, @PathVariable("granteeName") String granteeName) {
        TagDto unsharedTag = tagApplicationService.unshareTag(granteeName, UUID.fromString(tagId));
        return tagMapper.toResponse(unsharedTag);
    }

    @RequestMapping(method = PUT, path = "/{id}/name")
    @ResponseStatus(OK)
    Tag renameTag(@PathVariable("id") String tagId, @RequestBody String newName) {
        TagDto tagDto = tagApplicationService.renameTag(newName, UUID.fromString(tagId));
        return tagMapper.toResponse(tagDto);
    }

    @RequestMapping(method = PUT, path = "/{tagId}/metric/{metricId}/name")
    @ResponseStatus(OK)
    Tag renameMetric(@PathVariable("tagId") String tagId,
                     @PathVariable("metricId") String metricId,
                     @RequestBody String newName) {

        TagDto tagDto = tagApplicationService.renameMetric(
                newName,
                UUID.fromString(metricId),
                UUID.fromString(tagId)
        );

        return tagMapper.toResponse(tagDto);
    }

    @RequestMapping(method = POST, path = "/{tagId}/metric")
    @ResponseStatus(OK)
    Tag addMetric(@PathVariable("tagId") String tagId, @RequestBody Metric metric) {
        MetricDto metricDto = metricMapper.fromRequest(metric);

        TagDto tagDto = tagApplicationService
                .addMetricToTag(metricDto.name(), metricDto.type(), UUID.fromString(tagId));

        return tagMapper.toResponse(tagDto);
    }

    @RequestMapping(method = DELETE, path = "/{tagId}/metric/{metricId}")
    @ResponseStatus(OK)
    Tag deleteMetric(@PathVariable("tagId") String tagId, @PathVariable("metricId") String metricId) {
        TagDto tagDto = tagApplicationService
                .deleteMetric(UUID.fromString(metricId), UUID.fromString(tagId));

        return tagMapper.toResponse(tagDto);
    }

}
