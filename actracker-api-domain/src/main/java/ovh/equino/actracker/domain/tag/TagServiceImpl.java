package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public TagDto createTag(TagDto newTagData, User creator) {
        Tag createdTag = new Tag(new TagId(), newTagData, creator);
        TagDto tagDto = createdTag.toDto();
        tagRepository.add(tagDto);
        return tagDto;
    }

    @Override
    public TagDto updateTag(UUID tagId, TagDto updatedTagData, User updater) {
        Tag tag = getTagIfAuthorized(updater, tagId);
        tag.updateTo(updatedTagData);

        TagDto tagDto = tag.toDto();
        tagRepository.update(tagId, tagDto);
        return tagDto;
    }

    @Override
    public List<TagDto> getTags(User searcher) {
        return tagRepository.findAll(searcher);
    }

    @Override
    public void deleteTag(UUID tagId, User remover) {
        Tag tag = getTagIfAuthorized(remover, tagId);
        tag.delete();
        tagRepository.update(tagId, tag.toDto());
    }

    private Tag getTagIfAuthorized(User user, UUID tagId) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        Tag tag = Tag.fromDto(tagDto);

        if (tag.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(Tag.class, tagId);
        }
        return tag;
    }
}
