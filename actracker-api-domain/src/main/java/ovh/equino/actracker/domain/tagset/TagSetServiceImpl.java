package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

class TagSetServiceImpl implements TagSetService {

    private final TagSetRepository tagSetRepository;
    private final TagSetSearchEngine tagSetSearchEngine;
    private final TagRepository tagRepository;

    TagSetServiceImpl(TagSetRepository tagSetRepository,
                      TagSetSearchEngine tagSetSearchEngine,
                      TagRepository tagRepository) {

        this.tagSetRepository = tagSetRepository;
        this.tagSetSearchEngine = tagSetSearchEngine;
        this.tagRepository = tagRepository;
    }

    @Override
    public TagSetDto createTagSet(TagSetDto newTagSetData, User creator) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, creator);
        TagSet tagSet = TagSet.create(newTagSetData, creator, tagsExistenceVerifier);
        tagSetRepository.add(tagSet.forStorage());
        return tagSet.forClient();
    }

    @Override
    public TagSetDto updateTagSet(UUID tagSetId, TagSetDto updatedTagSetData, User updater) {
        TagSet tagSet = getTagSetIfAuthorized(updater, tagSetId);
        tagSet.updateTo(updatedTagSetData);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient();
    }

    @Override
    public EntitySearchResult<TagSetDto> searchTagSets(EntitySearchCriteria searchCriteria) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, searchCriteria.searcher());

        EntitySearchResult<TagSetDto> searchResult = tagSetSearchEngine.findTagSets(searchCriteria);
        List<TagSetDto> resultForClient = searchResult.results().stream()
                .map(tagSet -> TagSet.fromStorage(tagSet, tagsExistenceVerifier))
                .map(TagSet::forClient)
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    @Override
    public void deleteTagSet(UUID tagSetId, User remover) {
        TagSet tagSet = getTagSetIfAuthorized(remover, tagSetId);
        tagSet.delete();
        tagSetRepository.update(tagSetId, tagSet.forStorage());
    }

    private TagSet getTagSetIfAuthorized(User user, UUID tagSetId) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, user);

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        if (tagSet.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(TagSet.class, tagSetId);
        }
        return tagSet;
    }
}
