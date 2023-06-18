package ovh.equino.actracker.rest.spring.tag;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.EntitySearchCriteriaBuilder;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.share.Share;
import ovh.equino.actracker.rest.spring.share.ShareMapper;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tag")
class TagController {

    private final TagService tagService;
    private final IdentityProvider identityProvider;

    private final TagMapper tagMapper = new TagMapper();
    private final ShareMapper shareMapper = new ShareMapper();

    TagController(TagService tagService, IdentityProvider identityProvider) {
        this.tagService = tagService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Tag createTag(@RequestBody Tag tag) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        TagDto tagDto = tagMapper.fromRequest(tag);
        TagDto createdTag = tagService.createTag(tagDto, requester);

        return tagMapper.toResponse(createdTag);
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(OK)
    Tag updateTag(@PathVariable("id") String id, @RequestBody Tag tag) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        TagDto tagDto = tagMapper.fromRequest(tag);
        TagDto updatedTag = tagService.updateTag(UUID.fromString(id), tagDto, requester);

        return tagMapper.toResponse(updatedTag);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    List<Tag> resolveTags(@RequestParam(name = "ids", required = false) String tagIds) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        Set<UUID> parsedIds = tagMapper.parseIds(tagIds);
        List<TagDto> foundTags = tagService.getTags(parsedIds, requester);
        return tagMapper.toResponse(foundTags);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Tag> searchTags(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "excludedTags", required = false) String excludedTags) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteriaBuilder()
                .withSearcher(requester)
                .withPageId(pageId)
                .withPageSize(pageSize)
                .withTerm(term)
                .withExcludedIdsJointWithComma(excludedTags)
                .build();

        EntitySearchResult<TagDto> searchResult = tagService.searchTags(searchCriteria);
        return tagMapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    void deleteTag(@PathVariable("id") String id) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        tagService.deleteTag(UUID.fromString(id), requester);
    }

    @RequestMapping(method = POST, path = "/{id}/share")
    @ResponseStatus(OK)
    Tag shareTag(
            @PathVariable("id") String id,
            @RequestBody Share share) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        ovh.equino.actracker.domain.share.Share newShare = shareMapper.fromRequest(share);

        TagDto sharedTag = tagService.shareTag(UUID.fromString(id), newShare, requester);
        return tagMapper.toResponse(sharedTag);
    }
}
