package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.tagset.TagSet;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetRepository;
import ovh.equino.actracker.domain.tagset.TagSetSearchEngine;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.UUID;

public class TagSetApplicationService {

    private final TagSetRepository tagSetRepository;
    private final TagSetSearchEngine tagSetSearchEngine;
    private final TagRepository tagRepository;
    private final IdentityProvider identityProvider;

    TagSetApplicationService(TagSetRepository tagSetRepository,
                             TagSetSearchEngine tagSetSearchEngine,
                             TagRepository tagRepository,
                             IdentityProvider identityProvider) {

        this.tagSetRepository = tagSetRepository;
        this.tagSetSearchEngine = tagSetSearchEngine;
        this.tagRepository = tagRepository;
        this.identityProvider = identityProvider;
    }

    public TagSetDto createTagSet(TagSetDto newTagSetData) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, creator);

        TagSet tagSet = TagSet.create(newTagSetData, creator, tagsExistenceVerifier);
        tagSetRepository.add(tagSet.forStorage());
        return tagSet.forClient(creator);
    }

    public EntitySearchResult<TagSetDto> searchTagSets(TagSetsSearchQuery tagSetsSearchQuery) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, searcher);

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                tagSetsSearchQuery.pageSize(),
                tagSetsSearchQuery.pageId(),
                tagSetsSearchQuery.term(),
                null,
                null,
                tagSetsSearchQuery.excludeFilter(),
                null,
                null
        );
        EntitySearchResult<TagSetDto> searchResult = tagSetSearchEngine.findTagSets(searchCriteria);
        List<TagSetDto> resultForClient = searchResult.results().stream()
                .map(tagSet -> TagSet.fromStorage(tagSet, tagsExistenceVerifier))
                .map(tagSet -> tagSet.forClient(searchCriteria.searcher()))
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public TagSetDto renameTagSet(String newName, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.rename(newName, updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient(updater);
    }

    public TagSetDto addTagToSet(UUID tagId, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.assignTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient(updater);
    }

    public TagSetDto removeTagFromSet(UUID tagId, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.removeTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        return tagSet.forClient(updater);
    }

    public void deleteTagSet(UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, remover);
        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.delete(remover);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
    }
}
