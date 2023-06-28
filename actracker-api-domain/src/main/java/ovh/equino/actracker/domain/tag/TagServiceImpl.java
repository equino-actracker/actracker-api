package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TenantRepository tenantRepository;
    private final TagSearchEngine tagSearchEngine;
    private final TagNotifier tagNotifier;

    TagServiceImpl(TagRepository tagRepository,
                   TenantRepository tenantRepository,
                   TagSearchEngine tagSearchEngine,
                   TagNotifier tagNotifier) {

        this.tagRepository = tagRepository;
        this.tenantRepository = tenantRepository;
        this.tagSearchEngine = tagSearchEngine;
        this.tagNotifier = tagNotifier;
    }

    @Override
    public TagDto createTag(TagDto newTagData, User creator) {
        Tag tag = Tag.create(newTagData, creator);
        tagRepository.add(tag.forStorage());
        tagNotifier.notifyChanged(tag.forChangeNotification());
        return tag.forClient(creator);
    }

    @Override
    public TagDto updateTag(UUID tagId, TagDto updatedTagData, User updater) {
        Tag tag = getTagIfAuthorized(updater, tagId);
        tag.updateTo(updatedTagData, updater);
        tagRepository.update(tagId, tag.forStorage());
        tagNotifier.notifyChanged(tag.forChangeNotification());
        return tag.forClient(updater);
    }

    @Override
    public List<TagDto> getTags(Set<UUID> tagIds, User searcher) {
        return tagRepository.findByIds(tagIds, searcher).stream()
                .map(Tag::fromStorage)
                .map(tag -> tag.forClient(searcher))
                .toList();
    }

    @Override
    public EntitySearchResult<TagDto> searchTags(EntitySearchCriteria searchCriteria) {
        EntitySearchResult<TagDto> searchResult = tagSearchEngine.findTags(searchCriteria);
        List<TagDto> resultForClient = searchResult.results().stream()
                .map(Tag::fromStorage)
                .map(tag -> tag.forClient(searchCriteria.searcher()))
                .toList();
        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    @Override
    public void deleteTag(UUID tagId, User remover) {
        Tag tag = getTagIfAuthorized(remover, tagId);
        tag.delete(remover);
        tagRepository.update(tagId, tag.forStorage());
        tagNotifier.notifyChanged(tag.forChangeNotification());
    }

    @Override
    public TagDto shareTag(UUID tagId, Share share, User granter) {
        Tag tag = getTagIfAuthorized(granter, tagId);
        Share newShare = tenantRepository.findByUsername(share.granteeName())
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(share.granteeName()));

        tag.share(newShare, granter);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(granter);
    }

    private Tag getTagIfAuthorized(User user, UUID tagId) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        Tag tag = Tag.fromStorage(tagDto);

        if (tag.isNotAccessibleFor(user)) {
            throw new EntityNotFoundException(Tag.class, tagId);
        }
        return tag;
    }
}
