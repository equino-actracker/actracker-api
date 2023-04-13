package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagSearchEngine tagSearchEngine;
    private final TagNotifier tagNotifier;

    TagServiceImpl(TagRepository tagRepository, TagSearchEngine tagSearchEngine, TagNotifier tagNotifier) {
        this.tagRepository = tagRepository;
        this.tagSearchEngine = tagSearchEngine;
        this.tagNotifier = tagNotifier;
    }

    @Override
    public TagDto createTag(TagDto newTagData, User creator) {
        Tag tag = Tag.create(newTagData, creator);
        tagRepository.add(tag.forStorage());
        tagNotifier.notifyChanged(tag.forChangeNotification());
        return tag.forClient();
    }

    @Override
    public TagDto updateTag(UUID tagId, TagDto updatedTagData, User updater) {
        Tag tag = getTagIfAuthorized(updater, tagId);
        tag.updateTo(updatedTagData);
        tagRepository.update(tagId, tag.forStorage());
        tagNotifier.notifyChanged(tag.forChangeNotification());
        return tag.forClient();
    }

    @Override
    public List<TagDto> getTags(Set<UUID> tagIds, User searcher) {
        List<Tag> tags = tagRepository.findByIds(tagIds, searcher).stream()
                .map(Tag::fromStorage)
                .toList();
        return tags.stream()
                .map(Tag::forClient)
                .toList();
    }

    @Override
    public TagSearchResult searchTags(TagSearchCriteria searchCriteria) {
        return tagSearchEngine.findTags(searchCriteria);
    }

    @Override
    public void deleteTag(UUID tagId, User remover) {
        Tag tag = getTagIfAuthorized(remover, tagId);
        tag.delete();
        tagRepository.update(tagId, tag.forStorage());
        tagNotifier.notifyChanged(tag.forChangeNotification());
    }

    private Tag getTagIfAuthorized(User user, UUID tagId) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        Tag tag = Tag.fromStorage(tagDto);

        if (tag.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(Tag.class, tagId);
        }
        return tag;
    }
}
