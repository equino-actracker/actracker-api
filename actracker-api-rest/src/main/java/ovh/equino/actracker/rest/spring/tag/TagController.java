package ovh.equino.actracker.rest.spring.tag;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.tag.*;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.share.Share;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tag")
class TagController {

    private final TagApplicationService tagApplicationService;

    private final TagMapper tagMapper = new TagMapper();

    TagController(TagApplicationService tagApplicationService) {
        this.tagApplicationService = tagApplicationService;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Tag createTag(@RequestBody Tag tag) {
        List<MetricAssignment> assignedMetric = requireNonNullElse(tag.metrics(), new ArrayList<Metric>())
                .stream()
                .map(metric -> new MetricAssignment(metric.name(), metric.type()))
                .toList();
        List<String> grantedShares = requireNonNullElse(tag.shares(), new ArrayList<Share>())
                .stream()
                .map(Share::granteeName)
                .toList();
        CreateTagCommand createTagCommand = new CreateTagCommand(tag.name(), assignedMetric, grantedShares);
        TagResult createdTag = tagApplicationService.createTag(createTagCommand);

        return toResponse(createdTag);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    List<Tag> getTags(@RequestParam(name = "ids", required = false) String tagIds) {
        Set<UUID> parsedIds = tagMapper.parseIds(tagIds);
        List<TagResult> tagResults = tagApplicationService.resolveTags(parsedIds);
        return tagResults.stream()
                .map(this::toResponse)
                .toList();
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Tag> searchTags(@RequestParam(name = "pageId", required = false) String pageId,
                                   @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                   @RequestParam(name = "term", required = false) String term,
                                   @RequestParam(name = "excludedTags", required = false) String excludedTags) {

        SearchTagsQuery searchTagsQuery = new SearchTagsQuery(
                pageSize,
                pageId,
                term,
                tagMapper.parseIds(excludedTags)
        );
        SearchResult<TagResult> searchResult = tagApplicationService.searchTags(searchTagsQuery);
        List<Tag> foundResults = searchResult.results().stream()
                .map(this::toResponse)
                .toList();
        return new SearchResponse<>(searchResult.nextPageId(), foundResults);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    void deleteTag(@PathVariable("id") String id) {
        tagApplicationService.deleteTag(UUID.fromString(id));
    }

    @RequestMapping(method = POST, path = "/{id}/share")
    @ResponseStatus(OK)
    Tag addShareToTag(@PathVariable("id") String id,
                      @RequestBody Share share) {

        TagResult updatedTag = tagApplicationService.shareTag(share.granteeName(), UUID.fromString(id));
        return toResponse(updatedTag);
    }

    @RequestMapping(method = DELETE, path = "/{id}/share/{granteeName}")
    @ResponseStatus(OK)
    Tag removeShareFromTag(@PathVariable("id") String tagId,
                           @PathVariable("granteeName") String granteeName) {

        TagResult updatedTag = tagApplicationService.unshareTag(granteeName, UUID.fromString(tagId));
        return toResponse(updatedTag);
    }

    @RequestMapping(method = PUT, path = "/{id}/name")
    @ResponseStatus(OK)
    Tag replaceTagName(@PathVariable("id") String tagId,
                       @RequestBody String newName) {

        TagResult updatedTag = tagApplicationService.renameTag(newName, UUID.fromString(tagId));
        return toResponse(updatedTag);
    }

    @RequestMapping(method = PUT, path = "/{tagId}/metric/{metricId}/name")
    @ResponseStatus(OK)
    Tag replaceMetricName(@PathVariable("tagId") String tagId,
                          @PathVariable("metricId") String metricId,
                          @RequestBody String newName) {

        TagResult updatedTag = tagApplicationService.renameMetric(
                newName,
                UUID.fromString(metricId),
                UUID.fromString(tagId)
        );

        return toResponse(updatedTag);
    }

    @RequestMapping(method = POST, path = "/{tagId}/metric")
    @ResponseStatus(OK)
    Tag createMetric(@PathVariable("tagId") String tagId,
                     @RequestBody Metric metric) {

        TagResult updatedTag = tagApplicationService
                .addMetricToTag(metric.name(), metric.type(), UUID.fromString(tagId));

        return toResponse(updatedTag);
    }

    @RequestMapping(method = DELETE, path = "/{tagId}/metric/{metricId}")
    @ResponseStatus(OK)
    Tag deleteMetric(@PathVariable("tagId") String tagId,
                     @PathVariable("metricId") String metricId) {

        TagResult updatedTag = tagApplicationService
                .deleteMetric(UUID.fromString(metricId), UUID.fromString(tagId));

        return toResponse(updatedTag);
    }

    private Tag toResponse(TagResult tagResult) {
        List<Metric> metrics = tagResult.metrics().stream()
                .map(this::toResponse)
                .toList();
        List<Share> shares = tagResult.shares().stream()
                .map(Share::new)
                .toList();
        return new Tag(tagResult.id().toString(), tagResult.name(), metrics, shares);
    }

    private Metric toResponse(MetricResult metricResult) {
        return new Metric(metricResult.id().toString(), metricResult.name(), metricResult.type());
    }
}
