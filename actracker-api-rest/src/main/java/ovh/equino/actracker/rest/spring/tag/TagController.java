package ovh.equino.actracker.rest.spring.tag;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/tag")
class TagController {

    private final TagService tagService;
    private final IdentityProvider identityProvider;
    private final TagMapper mapper = new TagMapper();

    TagController(TagService tagService, IdentityProvider identityProvider) {
        this.tagService = tagService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Tag createTag(@RequestBody Tag tag) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        TagDto tagDto = mapper.fromRequest(tag);
        TagDto createdTag = tagService.createTag(tagDto, requester);

        return mapper.toResponse(createdTag);
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(OK)
    Tag updateTag(@PathVariable("id") String id, @RequestBody Tag tag) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        TagDto tagDto = mapper.fromRequest(tag);
        TagDto updatedTag = tagService.updateTag(UUID.fromString(id), tagDto, requester);

        return mapper.toResponse(updatedTag);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    List<Tag> getTags() {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        List<TagDto> tags = tagService.getTags(requester);
        return mapper.toResponse(tags);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    void deleteTag(@PathVariable("id") String id) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        tagService.deleteTag(UUID.fromString(id), requester);
    }
}
