package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.UUID;

public class TagSetApplicationService {

    private final TagSetRepository tagSetRepository;
    private final TagSetDataSource tagSetDataSource;
    private final TagSetSearchEngine tagSetSearchEngine;
    private final TagSetNotifier tagSetNotifier;
    private final TagRepository tagRepository;
    private final IdentityProvider identityProvider;

    public TagSetApplicationService(TagSetRepository tagSetRepository,
                                    TagSetDataSource tagSetDataSource,
                                    TagSetSearchEngine tagSetSearchEngine,
                                    TagSetNotifier tagSetNotifier,
                                    TagRepository tagRepository,
                                    IdentityProvider identityProvider) {

        this.tagSetRepository = tagSetRepository;
        this.tagSetDataSource = tagSetDataSource;
        this.tagSetSearchEngine = tagSetSearchEngine;
        this.tagSetNotifier = tagSetNotifier;
        this.tagRepository = tagRepository;
        this.identityProvider = identityProvider;
    }

    public TagSetResult createTagSet(CreateTagSetCommand createTagSetCommand) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagSetDto newTagSetData = new TagSetDto(createTagSetCommand.name(), createTagSetCommand.tags());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, creator);

        TagSet tagSet = TagSet.create(newTagSetData, creator, tagsExistenceVerifier);
        tagSetRepository.add(tagSet.forStorage());
        TagSetDto tagSetResult = tagSet.forClient(creator);

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

//        return toTagSetResult(tagSetResult);
        return tagSetDataSource.find(tagSet.id(), creator)
                .map(this::toTagSetResult)
                .orElseThrow(() -> {
                    String message = "Could not find created tag set with ID=%s".formatted(tagSet.id());
                    return new RuntimeException(message);
                });
    }

    public SearchResult<TagSetResult> searchTagSets(SearchTagSetsQuery searchTagSetsQuery) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, searcher);

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                searchTagSetsQuery.pageSize(),
                searchTagSetsQuery.pageId(),
                searchTagSetsQuery.term(),
                null,
                null,
                searchTagSetsQuery.excludeFilter(),
                null,
                null
        );
        EntitySearchResult<TagSetDto> searchResult = tagSetSearchEngine.findTagSets(searchCriteria);
        List<TagSetResult> resultForClient = searchResult.results().stream()
                .map(tagSet -> TagSet.fromStorage(tagSet, tagsExistenceVerifier))
                .map(tagSet -> tagSet.forClient(searchCriteria.searcher()))
                .map(this::toTagSetResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public TagSetResult renameTagSet(String newName, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);
        tagSet.rename(newName, updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        TagSetDto tagSetResult = tagSet.forClient(updater);

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return toTagSetResult(tagSetResult);
    }

    public TagSetResult addTagToSet(UUID tagId, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);
        tagSet.assignTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        TagSetDto tagSetResult = tagSet.forClient(updater);

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return toTagSetResult(tagSetResult);
    }

    public TagSetResult removeTagFromSet(UUID tagId, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);
        tagSet.removeTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());
        TagSetDto tagSetResult = tagSet.forClient(updater);

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return toTagSetResult(tagSetResult);
    }

    public void deleteTagSet(UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, remover);

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        TagSet tagSet = TagSet.fromStorage(tagSetDto, tagsExistenceVerifier);

        tagSet.delete(remover);
        tagSetRepository.update(tagSetId, tagSet.forStorage());

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());
    }

    private TagSetResult toTagSetResult(TagSetDto tagSetResult) {
        return new TagSetResult(tagSetResult.id(), tagSetResult.name(), tagSetResult.tags());
    }
}
