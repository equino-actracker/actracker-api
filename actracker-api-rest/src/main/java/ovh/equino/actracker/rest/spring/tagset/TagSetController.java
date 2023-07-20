package ovh.equino.actracker.rest.spring.tagset;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.tagset.CreateTagSetCommand;
import ovh.equino.actracker.application.tagset.SearchTagSetsQuery;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;
import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.tag.Tag;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tag-set")
class TagSetController {

    private final TagSetApplicationService tagSetApplicationService;
    private final TagSetMapper tagSetMapper = new TagSetMapper();

    TagSetController(TagSetApplicationService tagSetApplicationService) {
        this.tagSetApplicationService = tagSetApplicationService;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    TagSet createTagSet(@RequestBody TagSet tagSet) {
        CreateTagSetCommand createTagSetCommand = new CreateTagSetCommand(
                tagSet.name(),
                tagSetMapper.stringsToUuids(tagSet.tags())
        );
        TagSetResult createdTagSet = tagSetApplicationService.createTagSet(createTagSetCommand);
        return toResponse(createdTagSet);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<TagSet> searchTagSets(@RequestParam(name = "pageId", required = false) String pageId,
                                         @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                         @RequestParam(name = "term", required = false) String term,
                                         @RequestParam(name = "excludedTagSets", required = false) String excludedTagSets) {

        SearchTagSetsQuery searchTagSetsQuery = new SearchTagSetsQuery(
                pageSize,
                pageId,
                term,
                tagSetMapper.parseIds(excludedTagSets)
        );
        SearchResult<TagSetResult> searchResult = tagSetApplicationService.searchTagSets(searchTagSetsQuery);
        List<TagSet> foundResults = searchResult.results().stream()
                .map(this::toResponse)
                .toList();
        return new SearchResponse<>(searchResult.nextPageId(), foundResults);
    }

    @RequestMapping(method = DELETE, path = "/{tagSetId}")
    @ResponseStatus(OK)
    void deleteTagSet(@PathVariable("tagSetId") String tagSetId) {
        tagSetApplicationService.deleteTagSet(UUID.fromString(tagSetId));
    }

    @RequestMapping(method = PUT, path = "/{tagSetId}/name")
    @ResponseStatus(OK)
    TagSet replaceTagSetName(@PathVariable("tagSetId") String tagSetId,
                             @RequestBody String newName) {

        TagSetResult updatedTagSet = tagSetApplicationService.renameTagSet(newName, UUID.fromString(tagSetId));
        return toResponse(updatedTagSet);
    }

    @RequestMapping(method = POST, path = "/{tagSetId}/tag")
    @ResponseStatus(OK)
    TagSet addTagToTagSet(@PathVariable("tagSetId") String tagSetId,
                          @RequestBody Tag newTag) {

        TagSetResult updatedTagSet = tagSetApplicationService.addTagToSet(
                UUID.fromString(newTag.id()),
                UUID.fromString(tagSetId)
        );
        return toResponse(updatedTagSet);
    }

    @RequestMapping(method = DELETE, path = "/{tagSetId}/tag/{tagId}")
    @ResponseStatus(OK)
    TagSet removeTagFromTagSet(@PathVariable("tagSetId") String tagSetId,
                               @PathVariable(name = "tagId") String tagId) {

        TagSetResult updatedTagSet = tagSetApplicationService.removeTagFromSet(
                UUID.fromString(tagId),
                UUID.fromString(tagSetId)
        );
        return toResponse(updatedTagSet);
    }

    private TagSet toResponse(TagSetResult updatedTagSet) {
        return new TagSet(
                tagSetMapper.uuidToString(updatedTagSet.id()),
                updatedTagSet.name(),
                tagSetMapper.uuidsToStrings(updatedTagSet.tags())
        );
    }

}
