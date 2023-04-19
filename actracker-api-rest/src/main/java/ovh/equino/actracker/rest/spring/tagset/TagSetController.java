package ovh.equino.actracker.rest.spring.tagset;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tag-set")
class TagSetController {

    private final TagSetService tagSetService;
    private final IdentityProvider identityProvider;
    private final TagSetMapper mapper = new TagSetMapper();

    TagSetController(TagSetService tagSetService, IdentityProvider identityProvider) {
        this.tagSetService = tagSetService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    TagSet createTagSet(@RequestBody TagSet tagSet) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        TagSetDto tagSetDto = mapper.fromRequest(tagSet);
        TagSetDto createdTagSet = tagSetService.createTagSet(tagSetDto, requester);

        return mapper.toResponse(createdTagSet);
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(OK)
    TagSet updateTagSet(@PathVariable("id") String id, @RequestBody TagSet tagSet) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        TagSetDto tagSetDto = mapper.fromRequest(tagSet);
        TagSetDto updatedTagSet = tagSetService.updateTagSet(UUID.fromString(id), tagSetDto, requester);

        return mapper.toResponse(updatedTagSet);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<TagSet> searchTagSets(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "excludedTagSets", required = false) String excludedTagSets) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = mapper.fromRequest(requester, pageId, pageSize, term, excludedTagSets);
        EntitySearchResult<TagSetDto> searchResult = tagSetService.searchTagSets(searchCriteria);
        return mapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    @ResponseStatus(OK)
    void deleteTagSet(@PathVariable("id") String id) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        tagSetService.deleteTagSet(UUID.fromString(id), requester);
    }
}
