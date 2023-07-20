package ovh.equino.actracker.rest.spring.tagset;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;
import ovh.equino.actracker.application.tagset.TagSetsSearchQuery;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.tag.Tag;

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
        TagSetDto tagSetDto = tagSetMapper.fromRequest(tagSet);
        TagSetDto createdTagSet = tagSetApplicationService.createTagSet(tagSetDto);
        return tagSetMapper.toResponse(createdTagSet);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<TagSet> searchTagSets(@RequestParam(name = "pageId", required = false) String pageId,
                                         @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                         @RequestParam(name = "term", required = false) String term,
                                         @RequestParam(name = "excludedTagSets", required = false) String excludedTagSets) {

        TagSetsSearchQuery tagSetsSearchQuery = new TagSetsSearchQuery(
                pageSize,
                pageId,
                term,
                tagSetMapper.parseIds(excludedTagSets)
        );

        EntitySearchResult<TagSetDto> searchResult = tagSetApplicationService.searchTagSets(tagSetsSearchQuery);
        return tagSetMapper.toResponse(searchResult);
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
        TagSetDto tagSet = tagSetApplicationService.renameTagSet(newName, UUID.fromString(tagSetId));
        return tagSetMapper.toResponse(tagSet);
    }

    @RequestMapping(method = POST, path = "/{tagSetId}/tag")
    @ResponseStatus(OK)
    TagSet addTagToTagSet(@PathVariable("tagSetId") String tagSetId,
                          @RequestBody Tag newTag) {

        TagSetDto tagSet = tagSetApplicationService.addTagToSet(
                UUID.fromString(newTag.id()),
                UUID.fromString(tagSetId)
        );

        return tagSetMapper.toResponse(tagSet);
    }

    @RequestMapping(method = DELETE, path = "/{tagSetId}/tag/{tagId}")
    @ResponseStatus(OK)
    TagSet removeTagFromTagSet(@PathVariable("tagSetId") String tagSetId,
                               @PathVariable(name = "tagId") String tagId) {

        TagSetDto tagSet = tagSetApplicationService.removeTagFromSet(
                UUID.fromString(tagId),
                UUID.fromString(tagSetId)
        );

        return tagSetMapper.toResponse(tagSet);
    }

}
