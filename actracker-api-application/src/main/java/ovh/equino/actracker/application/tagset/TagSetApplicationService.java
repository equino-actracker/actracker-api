package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.tagset.TagSet;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public class TagSetApplicationService {

    private final TagSetRepository tagSetRepository;
    private final TagRepository tagRepository;

    TagSetApplicationService(TagSetRepository tagSetRepository, TagRepository tagRepository) {
        this.tagSetRepository = tagSetRepository;
        this.tagRepository = tagRepository;
    }

    public TagSetDto renameTagSet(String newName, UUID tagSetId, User updater) {
        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.rename(newName, updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient(updater);
    }

    public TagSetDto addTagToSet(UUID tagId, UUID tagSetId, User updater) {
        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.assignTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient(updater);
    }

    public TagSetDto removeTagFromSet(UUID tagId, UUID tagSetId, User updater) {
        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.removeTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient(updater);
    }

    public void deleteTagSet(UUID tagSetId, User remover) {
        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, remover);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.delete(remover);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
    }
}
